package kr.co.smartsignal.api;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.module.annotations.ReactModule;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@ReactModule(name = StlApiModule.NAME)
public class StlApiModule extends ReactContextBaseJavaModule {
  private static final int OVERLAY_PERMISSION_REQUEST = 6000;
  private static final int IGNORE_BATTERY_OPT_PERMISSION_REQUEST = 6001;
  private static final String E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST";
  private static final String E_FAILED_TO_PERMIT_OVERLAY = "E_FAILED_TO_PERMIT_OVERLAY";
  private static final String E_FAILED_TO_PERMIT_BATTERY_OPT = "E_FAILED_TO_PERMIT_BATTERY_OPT";

  public static final String NAME = "common";
  private final ReactApplicationContext reactContext;

  private Promise mOverlayPromise;
  private Promise mIgnoreBatteryOptPromise;

  public StlApiModule(ReactApplicationContext context) {
    super(context);
    reactContext = context;
    reactContext.addActivityEventListener(new BaseActivityEventListener() {
      @Override
      public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
          case OVERLAY_PERMISSION_REQUEST:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              mOverlayPromise.resolve(canDrawOverlays());
            } else {
              mOverlayPromise.resolve(true);
            }
            break;
          case IGNORE_BATTERY_OPT_PERMISSION_REQUEST:
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              mIgnoreBatteryOptPromise.resolve(isIgnoringBatteryOptimizations());
            } else {
              mIgnoreBatteryOptPromise.resolve(true);
            }
            break;
          default:
            break;
        }
      }
    });
  }

  @Override
  @NonNull
  public String getName() {
        return NAME;
    }

  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    try {
      PackageInfo packageInfo = reactContext
        .getPackageManager()
        .getPackageInfo(reactContext.getPackageName(), 0);
      ApplicationInfo info = reactContext.getPackageManager()
        .getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
      String appName = (String) reactContext.getPackageManager().getApplicationLabel(info);
      constants.put("name", appName);
      constants.put("version", packageInfo.versionName);
      constants.put("buildVersion", packageInfo.versionCode);
      constants.put("identifier", packageInfo.packageName);
    } catch (Exception e) {
      constants.put("name", "");
      constants.put("version", "");
      constants.put("buildVersion", null);
      constants.put("identifier", "");
    } finally {
      constants.put("COLOR_MODE", getColorModeConstants());
    }
    return constants;
  }

  private Map<String, Object> getColorModeConstants() {
    final Map<String, Object> constants = new HashMap<>();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      constants.put("DEFAULT", ActivityInfo.COLOR_MODE_DEFAULT);
      constants.put("WIDE_COLOR_GAMUT", ActivityInfo.COLOR_MODE_WIDE_COLOR_GAMUT);
      constants.put("HDR", ActivityInfo.COLOR_MODE_HDR);
    } else {
      constants.put("DEFAULT", 0);
      constants.put("WIDE_COLOR_GAMUT", 1);
      constants.put("HDR", 2);
    }
    return constants;
  }

  @ReactMethod
  public void setColorMode(int colorMode, Promise promise) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Activity activity = getCurrentActivity();
      if (activity != null) {
        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            activity.getWindow().setColorMode(colorMode);
            promise.resolve(null);
          }
        });
      } else {
        promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
      }
    }
  }

  @ReactMethod
  public void getColorMode(Promise promise) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Activity activity = getCurrentActivity();
      if (activity != null) {
        promise.resolve(activity.getWindow().getColorMode());
      } else {
        promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
      }
    } else {
      promise.resolve(0);
    }
  }

  @ReactMethod
  public boolean canDrawOverlays() {
    boolean canDraw;
    Context context = reactContext.getApplicationContext();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      canDraw = Settings.canDrawOverlays(context);
    } else {
      canDraw = false;
    }
    return canDraw;
  }

  @ReactMethod
  public void reqOverlayPermissions(Promise promise) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      promise.resolve(true);
    } else {
      Context context = reactContext.getApplicationContext();

      Activity activity = getCurrentActivity();
      if (activity == null) {
        promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
        return;
      }

      mOverlayPromise = promise;
      try {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        } else {
          intent = new Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + context.getPackageName())
          );
        }
        activity.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST);
      } catch (Exception e) {
        mOverlayPromise.reject(E_FAILED_TO_PERMIT_OVERLAY, e);
        mOverlayPromise = null;
      }
    }
  }

  @ReactMethod
  public boolean isIgnoringBatteryOptimizations() {
    boolean isIgnored = false;
    Context context = reactContext.getApplicationContext();

    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      isIgnored = pm.isIgnoringBatteryOptimizations(context.getPackageName());
    }
    return isIgnored;
  }

  @ReactMethod
  @SuppressLint("BatteryLife")
  public void reqIgnoringBatteryOptimizations(Promise promise) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      promise.resolve(true);
    } else {
      Context context = reactContext.getApplicationContext();

      Activity activity = getCurrentActivity();
      if (activity == null) {
        promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
        return;
      }

      mIgnoreBatteryOptPromise = promise;
      try {
        Intent intent = new Intent(
          Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
          Uri.parse("package:" + context.getPackageName())
        );
        activity.startActivityForResult(intent, IGNORE_BATTERY_OPT_PERMISSION_REQUEST);
      } catch (Exception e) {
        mIgnoreBatteryOptPromise.reject(E_FAILED_TO_PERMIT_BATTERY_OPT, e);
        mIgnoreBatteryOptPromise = null;
      }
    }
  }

  @ReactMethod
  @SuppressLint("PackageManagerGetSignatures")
  public void getKeyHashes(Promise promise) {
    WritableArray array = Arguments.createArray();
    Context context = reactContext.getApplicationContext();

    PackageInfo packageInfo = null;
    try {
      packageInfo = context
        .getPackageManager()
        .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
    } catch (PackageManager.NameNotFoundException e) {
      promise.reject("not_get_package_info", "KeyHash is null.");
    }

    if (packageInfo != null) {
      for (Signature signature: packageInfo.signatures) {
        try {
          MessageDigest digest = MessageDigest.getInstance("SHA");
          digest.update(signature.toByteArray());
          array.pushString(Base64.encodeToString(digest.digest(), Base64.NO_WRAP));
        } catch (NoSuchAlgorithmException e) {
          promise.reject("", "Unable to get MessageDigest. signature=" + signature);
        }
      }

      // return value
      promise.resolve(array);
    }
  }
}
