package kr.co.smartsignal.api.ble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ReactModule(name = BluetoothModule.NAME)
public class BluetoothModule extends ReactContextBaseJavaModule {

  private static final int PERMISSION_SCAN_RESULT_CODE = 5001;
  private static final int PERMISSION_ADVERTISE_RESULT_CODE = 5002;

  private static final String E_ACTIVITY_DOES_NOT_EXIST = "E_ACTIVITY_DOES_NOT_EXIST";

  public static final String NAME = "ble";

  private final ReactContext mReactContext;
  private final Context mContext;
  private Activity mActivity;
  private final CoreBluetooth mCoreBluetooth;

  private Promise mScanPromise;

  public BluetoothModule(ReactApplicationContext context) {
    super(context);
    mReactContext = context;
    mContext = context.getApplicationContext();
    mActivity = getCurrentActivity();
    mCoreBluetooth = createCoreBluetooth();

    context.addActivityEventListener(createBluetoothActivityEventListener());
  }

  @NonNull
  @Override
  public String getName() {
    return NAME;
  }

  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put("BluetoothGattCharacteristic", getBluetoothGattCharacteristicConstants());
    constants.put("BluetoothGattDescriptor", getBluetoothGattDescriptorConstants());
    return constants;
  }

  private Map<String, Object> getBluetoothGattCharacteristicConstants() {
    final Map<String, Object> constants = new HashMap<>();
    final Map<String, Object> properties = new HashMap<>();
    final Map<String, Object> permissions = new HashMap<>();
    permissions.put("READ", BluetoothGattCharacteristic.PERMISSION_READ);
    permissions.put("READ_ENCRYPTED", BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED);
    permissions.put("READ_ENCRYPTED_MITM", BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM);
    permissions.put("WRITE", BluetoothGattCharacteristic.PERMISSION_WRITE);
    permissions.put("WRITE_ENCRYPTED", BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED);
    permissions.put("WRITE_ENCRYPTED_MITM", BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM);
    permissions.put("WRITE_SIGNED", BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED);
    permissions.put("WRITE_SIGNED_MITM", BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED_MITM);
    properties.put("BROADCAST", BluetoothGattCharacteristic.PROPERTY_BROADCAST);
    properties.put("EXTENDED_PROPS", BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS);
    properties.put("INDICATE", BluetoothGattCharacteristic.PROPERTY_INDICATE);
    properties.put("NOTIFY", BluetoothGattCharacteristic.PROPERTY_NOTIFY);
    properties.put("READ", BluetoothGattCharacteristic.PROPERTY_READ);
    properties.put("SIGNED_WRITE", BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE);
    properties.put("WRITE", BluetoothGattCharacteristic.PROPERTY_WRITE);
    properties.put("WRITE_NO_RESPONSE", BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);
    constants.put("PROPERTY", properties);
    constants.put("PERMISSION", permissions);
    return constants;
  }

  private Map<String, Object> getBluetoothGattDescriptorConstants() {
    final Map<String, Object> constants = new HashMap<>();
    final Map<String, Object> permissions = new HashMap<>();
    permissions.put("READ", BluetoothGattDescriptor.PERMISSION_READ);
    permissions.put("READ_ENCRYPTED", BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED);
    permissions.put("READ_ENCRYPTED_MITM", BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM);
    permissions.put("WRITE", BluetoothGattDescriptor.PERMISSION_WRITE);
    permissions.put("WRITE_ENCRYPTED", BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED);
    permissions.put("WRITE_ENCRYPTED_MITM", BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM);
    permissions.put("WRITE_SIGNED", BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED);
    permissions.put("WRITE_SIGNED_MITM", BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM);
    constants.put("PERMISSION", permissions);
    return constants;
  }

  private void sendEvent(String eventName, @Nullable WritableMap params) {
    mReactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }

  @ReactMethod
  public void addListener(String eventName) {
    // Set up any upstream listeners or background tasks as necessary
  }

  @ReactMethod
  public void removeListeners(Integer count) {
    // Remove upstream listeners, stop unnecessary background tasks
  }

  @ReactMethod
  public void startScan(Promise promise) {
    mScanPromise = promise;
    mCoreBluetooth.startScan(new ArrayList<>());
  }

  @ReactMethod
  public void startScanByCompanyId(int companyId, Promise promise) {
    mScanPromise = promise;
    List<ScanFilter> filters = new ArrayList<>();
    filters.add(new ScanFilter.Builder().setManufacturerData(companyId, new byte[]{}).build());
    mCoreBluetooth.startScan(filters);
  }

  @ReactMethod
  public void isDiscovering(Promise promise) {
     promise.resolve(mCoreBluetooth.isDiscovering());
  }

  @ReactMethod
  public void stopScan(Promise promise) {
    mScanPromise = promise;
    mCoreBluetooth.stopScan();
  }

  @ReactMethod
  public void connect(String identifier) {
    mCoreBluetooth.connect(mCoreBluetooth.getDevice(identifier));
  }

  @ReactMethod
  public void disconnect() {
    mCoreBluetooth.disconnect();
  }

  @ReactMethod
  public void requestAdvertisePermission(Promise promise) {
    if (isCurrentActivity(promise)) {
      mCoreBluetooth.requestAdvertisePermission();
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void requestScanPermissions(Promise promise) {
    if (isCurrentActivity(promise)) {
      mCoreBluetooth.requestScanPermission();
    }
    promise.resolve(null);
  }

  private boolean isCurrentActivity(Promise promise) {
    if (mActivity == null) {
      if (getCurrentActivity() != null) {
        mActivity = getCurrentActivity();
      } else {
        promise.reject(E_ACTIVITY_DOES_NOT_EXIST, "Activity doesn't exist");
        return false;
      }
    }
    return true;
  }

  protected ActivityEventListener createBluetoothActivityEventListener() {
    return new BaseActivityEventListener() {
      @Override
      public void onActivityResult(Activity activity, int requestCode, int resultCode, @Nullable Intent data) {
        Log.i("BluetoothModule", requestCode + ";");
        if (requestCode == PERMISSION_SCAN_RESULT_CODE) {
          Log.i("BluetoothModule", resultCode + ";");
          sendEvent("PermitBLEScan", null);
        } else if (requestCode == PERMISSION_ADVERTISE_RESULT_CODE) {
          sendEvent("PermitBLEAdvertise", null);
        }
      }
    };
  }

  protected CoreBluetooth createCoreBluetooth() {
    return new CoreBluetooth();
  }

  private class CoreBluetooth {

    private final static String TAG = "CoreBluetooth";

    private BluetoothAdapter mBluetoothAdapter;
    private final ScanCallback mScanCallback;
    private final Map<UUID, BluetoothDevice> devices;

    private BluetoothGatt mGatt;

    CoreBluetooth() {
      BluetoothManager mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
      devices = Collections.emptyMap();
      mScanCallback = createBluetoothScanCallback();

      if (mBluetoothManager != null) {
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
          Log.i(TAG, "Unable to initialize BluetoothAdapter.");
        }
      } else {
        Log.i(TAG, "Unable to initialize BluetoothManager.");
      }
    }

    public BluetoothDevice getDevice(String identifier) {
      return devices.get(UUID.fromString(identifier));
    }

    @SuppressLint("MissingPermission")
    public void startScan(List<ScanFilter> filters) {
      if (mBluetoothAdapter == null) {
        mScanPromise.reject("E_BLUETOOTH_ADAPTER_NOT_INIT", "Unable to initialize BluetoothAdapter.");
      }

      if (checkPermissions()) {
        if (mBluetoothAdapter.isDiscovering()) {
          mScanPromise.reject("E_BLUETOOTH_HAS_SCANNED", "Bluetooth already scanned.");
        }

        ScanSettings scanSettings = new ScanSettings.Builder()
          .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
          .build();

        mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, scanSettings, mScanCallback);

        Toast.makeText(mContext, "블루투스 스캔이 시작되었습니다.", Toast.LENGTH_LONG).show();

        mScanPromise.resolve(null);
      } else {
        mScanPromise.reject("E_BLUETOOTH_PERMISSION", "Bluetooth has no permission.");
      }
    }

    @SuppressLint("MissingPermission")
    public boolean isDiscovering() {
      if (mBluetoothAdapter != null ) {
        return mBluetoothAdapter.isDiscovering();
      }

      return false;
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
      if (mBluetoothAdapter == null) {
        mScanPromise.reject("E_BLUETOOTH_ADAPTER_NOT_INIT", "Unable to initialize BluetoothAdapter.");
      }

      if (checkPermissions()) {
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        Toast.makeText(mContext, "블루투스 스캔이 중지되었습니다.", Toast.LENGTH_LONG).show();

        mScanPromise.resolve(null);
      } else {
        mScanPromise.reject("E_BLUETOOTH_PERMISSION", "Bluetooth has no permission.");
      }
    }

    @SuppressLint("MissingPermission")
    public void connect(BluetoothDevice device) {
      if (!checkPermissions()) {
        return;
      }

      device.connectGatt(mContext, false, new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
          switch (newState) {
            case BluetoothProfile.STATE_DISCONNECTED:
              if (status == BluetoothGatt.GATT_SUCCESS) {
                mGatt = null;
              }
              break;
            case BluetoothProfile.STATE_CONNECTED:
              if (status == BluetoothGatt.GATT_SUCCESS) {
                mGatt = gatt;
              } else {
                mGatt = null;
              }
              break;
          }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
          if (status == BluetoothGatt.GATT_SUCCESS) {
            WritableMap params = Arguments.createMap();
            WritableMap descriptors = Arguments.createMap();
            for (BluetoothGattDescriptor descriptor: characteristic.getDescriptors()) {
              descriptors.putString(descriptor.getUuid().toString(), Base64.encodeToString(descriptor.getValue(), Base64.DEFAULT));
            }
            params.putString("uuid", characteristic.getUuid().toString());
            params.putMap("descriptors", descriptors);
            sendEvent("CharacteristicRead", params);
          }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
          if (status == BluetoothGatt.GATT_SUCCESS) {
            WritableMap params = Arguments.createMap();
            params.putString("uuid", descriptor.getUuid().toString());
            params.putString("data", Base64.encodeToString(descriptor.getValue(), Base64.DEFAULT));
            sendEvent("DescriptorRead", params);
          }
        }
      });
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
      if (checkPermissions() && mGatt != null) {
        mGatt.disconnect();
        mGatt = null;
      }
    }

    @SuppressLint("MissingPermission")
    public void writeCharacteristic(UUID uuid, int properties, byte[] data) {
      if (checkPermissions() && mGatt != null) {
        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(uuid, properties, BluetoothGattCharacteristic.PERMISSION_WRITE);
        characteristic.setValue(data);
        mGatt.writeCharacteristic(characteristic);
      }
    }

    @SuppressLint("MissingPermission")
    public void readCharacteristic(UUID uuid, int properties) {
      if (checkPermissions() && mGatt != null) {
        mGatt.readCharacteristic(new BluetoothGattCharacteristic(uuid, properties, BluetoothGattCharacteristic.PERMISSION_READ));
      }
    }

    @SuppressLint("MissingPermission")
    public void writeDescriptor(UUID uuid, byte[] data) {
      if (checkPermissions() && mGatt != null) {
        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(uuid, BluetoothGattDescriptor.PERMISSION_WRITE);
        descriptor.setValue(data);
        mGatt.writeDescriptor(descriptor);
      }
    }

    @SuppressLint("MissingPermission")
    public void readDescriptor(UUID uuid) {
      if (checkPermissions() && mGatt != null) {
        mGatt.readDescriptor(new BluetoothGattDescriptor(uuid, BluetoothGattDescriptor.PERMISSION_READ));
      }
    }

    protected void requestAdvertisePermission() {
      if (!checkAdvertisePermissions()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          String[] permissions = new String[] {
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
          };
          ActivityCompat.requestPermissions(mActivity, permissions, PERMISSION_ADVERTISE_RESULT_CODE);
        }
      }
    }

    protected void requestScanPermission() {
      if (!checkPermissions()) {
        ActivityCompat.requestPermissions(mActivity, getScanPermissions(), PERMISSION_SCAN_RESULT_CODE);
      }
    }

    private String[] getScanPermissions() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return new String[] {
          Manifest.permission.BLUETOOTH_SCAN,
          Manifest.permission.BLUETOOTH_CONNECT,
          Manifest.permission.ACCESS_FINE_LOCATION
        };
      } else {
        return new String[] {
          Manifest.permission.ACCESS_FINE_LOCATION
        };
      }
    }

    private boolean checkAdvertisePermissions() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        boolean bluetoothScan = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED;
        boolean bluetoothConnect = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        return bluetoothScan && bluetoothConnect;
      } else {
        return true;
      }
    }

    private boolean checkPermissions() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        boolean bluetoothScan = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        boolean bluetoothConnect = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        return bluetoothScan && bluetoothConnect;
      } else {
        return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
      }
    }

    protected ScanCallback createBluetoothScanCallback() {
      return new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
          BluetoothDevice newDevice = result.getDevice();
          ScanRecord record = result.getScanRecord();
          WritableMap params = Arguments.createMap();
          UUID identifier = UUID.randomUUID();
          byte[] bytes = record.getManufacturerSpecificData(0x0AB9);

          boolean is_duplicated = false;
          for (Map.Entry<UUID, BluetoothDevice> entry: devices.entrySet()) {
            if(entry.getValue().getAddress().equals(newDevice.getAddress())) {
              is_duplicated = true;
              identifier = entry.getKey();
            }
          }

          if (!is_duplicated) {
            devices.put(identifier, newDevice);
          }

          params.putString("identifier", identifier.toString());
          params.putString("name", record.getDeviceName());
          params.putInt("RSSI", result.getRssi());
          params.putInt("TxPowerLevel", record.getTxPowerLevel());
          if (bytes != null) {
            params.putString("ManufacturerSpecificData", Base64.encodeToString(bytes, Base64.DEFAULT));
          } else {
            params.putNull("ManufacturerSpecificData");
          }

          // emit event
          sendEvent("FoundBLEDevice", params);
        }
      };
    }
  }
}
