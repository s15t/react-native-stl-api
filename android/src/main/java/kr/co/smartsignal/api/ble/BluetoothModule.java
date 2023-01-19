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
import android.bluetooth.BluetoothGattService;
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
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
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

  private Promise mDiscoverServicesPromise;

  private Promise mReadCharacteristicPromise;
  private Promise mReadDescriptorPromise;
  private Promise mWriteCharacteristicPromise;
  private Promise mWriteDescriptorPromise;

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
    mCoreBluetooth.setCompanyIds(new ArrayList<>());
    mCoreBluetooth.startScan(new ArrayList<>());
  }

  @ReactMethod
  public void startScanByCompanyId(ReadableArray companyIds, Promise promise) {
    mScanPromise = promise;
    List<Integer> listCompanyId = new ArrayList<>();
    List<ScanFilter> filters = new ArrayList<>();
    int p = 0;
    while (p < companyIds.size()) {
      int companyId = companyIds.getInt(p++);
      listCompanyId.add(companyId);
      filters.add(new ScanFilter.Builder().setManufacturerData(companyId, new byte[]{}).build());
    }
    mCoreBluetooth.setCompanyIds(listCompanyId);
    mCoreBluetooth.startScan(filters);
  }

  @ReactMethod
  public void isDiscovering(Promise promise) {
     promise.resolve(mCoreBluetooth.isDiscovering());
  }

  @ReactMethod
  public void checkAdvertisePermission(Promise promise) {
    promise.resolve(mCoreBluetooth.checkAdvertisePermissions());
  }

  @ReactMethod
  public void checkScanPermission(Promise promise) {
    promise.resolve(mCoreBluetooth.checkPermissions());
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
  public void discoverServices(Promise promise) {
    mDiscoverServicesPromise = promise;
    mCoreBluetooth.discoverServices();
  }

  @ReactMethod
  public void writeCharacteristic(String serviceId, String uuid, String data, Promise promise) {
    mWriteCharacteristicPromise = promise;
    mCoreBluetooth.writeCharacteristic(UUID.fromString(serviceId), UUID.fromString(uuid), Base64.decode(data, Base64.DEFAULT));
  }

  @ReactMethod
  public void readCharacteristic(String serviceId, String uuid, Promise promise) {
    mReadCharacteristicPromise = promise;
    mCoreBluetooth.readCharacteristic(UUID.fromString(serviceId), UUID.fromString(uuid));
  }

  @ReactMethod
  public void writeDescriptor(String serviceId, String characteristicId, String uuid, String data, Promise promise) {
    mWriteDescriptorPromise = promise;
    mCoreBluetooth.writeDescriptor(
      UUID.fromString(serviceId),
      UUID.fromString(characteristicId),
      UUID.fromString(uuid),
      Base64.decode(data, Base64.DEFAULT)
    );
  }

  @ReactMethod
  public void readDescriptor(String serviceId, String characteristicId, String uuid, Promise promise) {
    mReadDescriptorPromise = promise;
    mCoreBluetooth.readDescriptor(
      UUID.fromString(serviceId),
      UUID.fromString(characteristicId),
      UUID.fromString(uuid)
    );
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
    private final Map<String, BluetoothDevice> devices;

    private BluetoothGatt mGatt;

    private List<Integer> companyIds;

    CoreBluetooth() {
      BluetoothManager mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
      devices = new HashMap<>();
      companyIds = new ArrayList<>();
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

    public void setCompanyIds(List<Integer> companyIds) {
      this.companyIds = companyIds;
    }

    public BluetoothDevice getDevice(String identifier) {
      return devices.get(identifier);
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

        Toast.makeText(mContext, "블루투스 스캔이 시작되었습니다.", Toast.LENGTH_SHORT).show();

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
        Toast.makeText(mContext, "블루투스 스캔이 중지되었습니다.", Toast.LENGTH_SHORT).show();
        devices.clear();

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
                sendEvent("Disconnected", null);
                mGatt = null;
              }
              break;
            case BluetoothProfile.STATE_CONNECTED:
              if (status == BluetoothGatt.GATT_SUCCESS) {
                mGatt = gatt;
                sendEvent("Connected", null);
              } else {
                sendEvent("Disconnected", null);
                mGatt = null;
              }
              break;
          }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
          super.onServicesDiscovered(gatt, status);
          if (status == BluetoothGatt.GATT_SUCCESS) {
            WritableMap params = Arguments.createMap();
            WritableArray services = Arguments.createArray();
            WritableArray characteristics = Arguments.createArray();
            WritableArray descriptors = Arguments.createArray();
            for (BluetoothGattService service: gatt.getServices()) {
              services.pushString(service.getUuid().toString());
              for (BluetoothGattCharacteristic characteristic: service.getCharacteristics()) {
                characteristics.pushString(characteristic.getUuid().toString());
                for (BluetoothGattDescriptor descriptor: characteristic.getDescriptors()) {
                  descriptors.pushString(descriptor.getUuid().toString());
                }
              }
            }
            params.putArray("serviceIds", services);
            params.putArray("characteristics", characteristics);
            params.putArray("descriptors", descriptors);
            if (mDiscoverServicesPromise != null) {
              mDiscoverServicesPromise.resolve(params);
            }
          } else {
            if (mDiscoverServicesPromise != null) {
              mDiscoverServicesPromise.reject("E_GATT_ERROR", "Cannot be found service(s).");
            }
          }
          mDiscoverServicesPromise = null;
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
          if (status == BluetoothGatt.GATT_SUCCESS) {
            WritableMap params = Arguments.createMap();
            params.putString("uuid", characteristic.getUuid().toString());
            params.putString("data", Base64.encodeToString(characteristic.getValue(), Base64.DEFAULT));
            params.putNull("descriptors");
            if (mReadCharacteristicPromise != null) {
              mReadCharacteristicPromise.resolve(params);
            }
          } else {
            if (mReadCharacteristicPromise != null) {
              mReadCharacteristicPromise.reject("E_GATT_ERROR", "Cannot be read characteristic.");
            }
          }
          mReadCharacteristicPromise = null;
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
          super.onCharacteristicWrite(gatt, characteristic, status);
          if (status == BluetoothGatt.GATT_SUCCESS) {
            if (mWriteCharacteristicPromise != null) {
              mWriteCharacteristicPromise.resolve(null);
            }
          } else {
            if (mWriteCharacteristicPromise != null) {
              mWriteCharacteristicPromise.reject("E_GATT_ERROR", "Cannot be written characteristic.");
            }
          }
          mWriteCharacteristicPromise = null;
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
          super.onCharacteristicChanged(gatt, characteristic);
          WritableMap params = Arguments.createMap();
          params.putString("uuid", characteristic.getUuid().toString());
          params.putString("data", Base64.encodeToString(characteristic.getValue(), Base64.DEFAULT));
          sendEvent("CharacteristicChanged", params);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
          if (status == BluetoothGatt.GATT_SUCCESS) {
            WritableMap params = Arguments.createMap();
            params.putString("uuid", descriptor.getUuid().toString());
            params.putString("data", Base64.encodeToString(descriptor.getValue(), Base64.DEFAULT));
            if (mReadDescriptorPromise != null) {
              mReadDescriptorPromise.resolve(params);
            }
          } else {
            if (mReadDescriptorPromise != null) {
              mReadDescriptorPromise.reject("E_GATT_ERROR", "Cannot be read descriptor.");
            }
          }
          mReadDescriptorPromise = null;
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
          super.onDescriptorWrite(gatt, descriptor, status);
          if (status == BluetoothGatt.GATT_SUCCESS) {
            if (mWriteDescriptorPromise != null) {
              mWriteDescriptorPromise.resolve(null);
            }
          } else {
            if (mWriteDescriptorPromise != null) {
              mWriteDescriptorPromise.reject("E_GATT_ERROR", "Cannot be written descriptor.");
            }
          }
          mWriteDescriptorPromise = null;
        }
      });
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
      if (checkPermissions() && mGatt != null) {
        mGatt.disconnect();
        mGatt = null;
        sendEvent("Disconnected", null);
      }
    }

    @SuppressLint("MissingPermission")
    public void discoverServices() {
      if (checkPermissions() && mGatt != null) {
        mGatt.discoverServices();
      }
    }

    @SuppressLint("MissingPermission")
    public void writeCharacteristic(UUID serviceId, UUID uuid, byte[] data) {
      if (checkPermissions() && mGatt != null) {
        BluetoothGattCharacteristic characteristic = mGatt.getService(serviceId).getCharacteristic(uuid);
        characteristic.setValue(data);
        mGatt.writeCharacteristic(characteristic);
      }
    }

    @SuppressLint("MissingPermission")
    public void readCharacteristic(UUID serviceId, UUID uuid) {
      if (checkPermissions() && mGatt != null) {
        mGatt.readCharacteristic(mGatt.getService(serviceId).getCharacteristic(uuid));
      }
    }

    @SuppressLint("MissingPermission")
    public void writeDescriptor(UUID serviceId, UUID characteristicId, UUID uuid, byte[] data) {
      if (checkPermissions() && mGatt != null) {
        BluetoothGattDescriptor descriptor = mGatt.getService(serviceId).getCharacteristic(characteristicId).getDescriptor(uuid);
        descriptor.setValue(data);
        mGatt.writeDescriptor(descriptor);
      }
    }

    @SuppressLint("MissingPermission")
    public void readDescriptor(UUID serviceId, UUID characteristicId, UUID uuid) {
      if (checkPermissions() && mGatt != null) {
        mGatt.readDescriptor(mGatt.getService(serviceId).getCharacteristic(characteristicId).getDescriptor(uuid));
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

    public boolean checkAdvertisePermissions() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED;
      } else {
        return true;
      }
    }

    public boolean checkPermissions() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        return ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
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
          String deviceName = record.getDeviceName();
          int RSSI = result.getRssi();
          int TxPowerLevel = record.getTxPowerLevel() == Integer.MIN_VALUE ? 0 : record.getTxPowerLevel();

          boolean is_duplicated = false;
          for (Map.Entry<String, BluetoothDevice> entry: devices.entrySet()) {
            if(entry.getValue().getAddress().equals(newDevice.getAddress())) {
              is_duplicated = true;
              identifier = UUID.fromString(entry.getKey());
            }
          }
          if (!is_duplicated) {
            devices.put(identifier.toString(), newDevice);
          }

          params.putString("identifier", identifier.toString());
          params.putString("name", deviceName);
          params.putInt("RSSI", RSSI);
          params.putInt("TxPowerLevel", TxPowerLevel);
          params.putNull("ManufacturerSpecificData");
          for (Integer companyId: companyIds) {
            byte[] bytes = record.getManufacturerSpecificData(companyId);
            if (bytes != null) {
              params.putString("ManufacturerSpecificData", Base64.encodeToString(bytes, Base64.DEFAULT));
            }
          }

          // emit event
          sendEvent("FoundBLEDevice", params);
        }
      };
    }
  }
}
