package kr.co.smartsignal.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
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
  public static final String NAME = "common";
  private final ReactApplicationContext reactContext;

  public StlApiModule(ReactApplicationContext context) {
    super(context);
    reactContext = context;
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
    }
    return constants;
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
