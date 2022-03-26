package kr.co.smartsignal.api.register;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

@ReactModule(name = RegisterAPIModule.NAME)
public class RegisterAPIModule extends ReactContextBaseJavaModule {
  public static final String NAME = "register";
  private final String TAG = "STLApiRegister";

  private BluetoothAdapter mBluetoothAdapter;
  private boolean isBLEFound;
  private Promise mFindBLEPromise;

  public RegisterAPIModule(ReactApplicationContext reactContext) {
    super(reactContext);

    // initialize BLE
    BluetoothManager mBluetoothManager = (BluetoothManager) reactContext.getSystemService(Context.BLUETOOTH_SERVICE);
    if (mBluetoothManager == null) {
      Log.i(TAG, "Unable to initialize BluetoothManager.");
    } else {
      mBluetoothAdapter = mBluetoothManager.getAdapter();
    }
  }

  private ScanCallback mScanCallback = new ScanCallback() {
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
      if (callbackType == ScanSettings.CALLBACK_TYPE_ALL_MATCHES || callbackType == ScanSettings.CALLBACK_TYPE_FIRST_MATCH) {
        int RSSI = result.getRssi();
        ScanRecord record = result.getScanRecord();
        WritableMap map = Arguments.createMap();

        if (record != null) {
          byte[] bytes = getManufacturerSpecificData(record);
          if (bytes != null && bytes.length > 0 && RSSI > -40) {
            isBLEFound = true;
            map.putInt("type", bytes[0]);
            map.putString("id", bytesToHexString(
              Arrays.copyOfRange(bytes, 1, 7)
            ));
            mFindBLEPromise.resolve(map);
          }
        }
      }
    }
  };

  @NonNull
  @Override
  public String getName() {
    return NAME;
  }

  @Nullable
  private byte[] getManufacturerSpecificData(ScanRecord record) {
    if (record.getManufacturerSpecificData(0x088C) != null) {
      return record.getManufacturerSpecificData(0x088C);
    } else if (record.getManufacturerSpecificData(0x0AB9) != null) {
      return record.getManufacturerSpecificData(0x0AB9);
    } else {
      return null;
    }
  }

  private String bytesToHexString(byte[] bytes) {
    char[] HexDecimals = "0123456789ABCDEF".toCharArray();
    char[] chars = new char[bytes.length * 2];
    for (int i = 0; i < bytes.length; i++) {
      int decimal = bytes[i] & 0xff;
      chars[i * 2] = HexDecimals[decimal >> 4];
      chars[(i * 2) + 1] = HexDecimals[decimal & 0x0f];
    }
    return new String(chars);
  }

  @ReactMethod
  public void getNearbyDevice(Promise promise) {
    isBLEFound = false;
    mFindBLEPromise = promise;

    if (mBluetoothAdapter == null) {
      promise.reject("not_initialize_ble", "Unable to initialize BluetoothAdapter.");
    } else {
      if (!mBluetoothAdapter.isDiscovering()) {
        ArrayList<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(new ScanFilter.Builder().setManufacturerData(0x088C, new byte[]{}).build());
        // filters.add(new ScanFilter.Builder().setManufacturerData(0x0AB9, new byte[]{}).build());
        ScanSettings settings = new ScanSettings.Builder()
          .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
          .setReportDelay(0)
          .build();
        mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, mScanCallback);
        Log.i(TAG, "BLE scan started.");

        // clear task
        new Timer().schedule(new TimerTask() {
          @Override
          public void run() {
            if (!isBLEFound) {
              promise.reject("not_found", "There were no devices.");
            }
            if (mBluetoothAdapter.isDiscovering()) {
              mBluetoothAdapter.getBluetoothLeScanner().flushPendingScanResults(mScanCallback);
              mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
            }
          }
        }, 3 * 1000);
      } else {
        promise.reject("already_scanning", "Be already scanning peripherals.");
      }
    }
  }
}
