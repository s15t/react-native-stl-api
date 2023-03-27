import { NativeEventEmitter, NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-stl-api' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

// @ts-expect-error
const isTurboModuleEnabled = global.__turboModuleProxy != null;

const CommonModule = isTurboModuleEnabled
  ? require('./NativeCommon').default
  : NativeModules.Common;
const common = CommonModule
  ? CommonModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

const BluetoothModule = isTurboModuleEnabled
  ? require('./NativeBluetoothModule').default
  : NativeModules.BluetoothModule;
const ble = BluetoothModule
  ? BluetoothModule
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
const bleEmitter = new NativeEventEmitter(ble);

type BluetoothGattCharacteristicProps = {
  PERMISSION: { [key: string]: number };
  PROPERTY: { [key: string]: number };
};

type BluetoothGattDescriptorProps = {
  PERMISSION: { [key: string]: number };
};

type BluetoothGattServicesProps = {
  serviceIds: string[];
  characteristics: string[];
  descriptors: string[];
};

type BluetoothGattReadCharacteristicProps = {
  uuid: string;
  data: string;
  descriptors: { [id: string]: string };
};

type BluetoothGattReadDescriptorProps = {
  uuid: string;
  data: string;
};

const BluetoothGattCharacteristic: BluetoothGattCharacteristicProps =
  ble.getConstants().BluetoothGattCharacteristic;

const BluetoothGattDescriptor: BluetoothGattDescriptorProps =
  ble.getConstants().BluetoothGattDescriptor;

const API = {
  ble: {
    requestAdvertisePermission: (): Promise<null> => {
      if (Platform.OS === 'android') {
        return ble.requestAdvertisePermission();
      }
      return Promise.resolve(null);
    },
    requestScanPermissions: (): Promise<null> => {
      if (Platform.OS === 'android') {
        return ble.requestScanPermissions();
      }
      return Promise.resolve(null);
    },
    startScan: (): Promise<void> => ble.startScan(),
    startScanByCompanyId: (companyIds: number[]): Promise<void> => {
      return ble.startScanByCompanyId(companyIds);
    },
    stopScan: (): Promise<void> => ble.stopScan(),
    checkAdvertisePermission: (): Promise<boolean> => {
      if (Platform.OS === 'android') {
        return ble.checkAdvertisePermission();
      }
      return Promise.resolve(true);
    },
    checkScanPermission: (): Promise<boolean> => {
      if (Platform.OS === 'android') {
        return ble.checkScanPermission();
      }
      return Promise.resolve(true);
    },
    isDiscovering: (): Promise<boolean> => {
      return ble.isDiscovering();
    },
    connect: (identifier: string) => ble.connect(identifier),
    disconnect: () => ble.disconnect(),
    ...Platform.select({
      android: {
        discoverServices: (): Promise<BluetoothGattServicesProps> => {
          return ble.discoverServices();
        },
      },
      ios: {
        discoverServices: (): Promise<string[]> => {
          return ble.discoverServices();
        },
        discoverCharacteristics: (uuid: string): Promise<string[]> => {
          return ble.discoverCharacteristics(uuid);
        },
        discoverDescriptors: (uuid: string): Promise<string[]> => {
          return ble.discoverDescriptors(uuid);
        },
      },
    }),
    writeCharacteristic: (
      serviceId: string,
      uuid: string,
      data: string
    ): Promise<void> => {
      return ble.writeCharacteristic(serviceId, uuid, data);
    },
    readCharacteristic: (
      serviceId: string,
      uuid: string
    ): Promise<BluetoothGattReadCharacteristicProps> => {
      return ble.readCharacteristic(serviceId, uuid);
    },
    writeDescriptor: (
      serviceId: string,
      characteristicId: string,
      uuid: string,
      data: string
    ): Promise<void> => {
      return ble.writeDescriptor(serviceId, characteristicId, uuid, data);
    },
    readDescriptor: (
      serviceId: string,
      characteristicId: string,
      uuid: string
    ): Promise<BluetoothGattReadDescriptorProps> => {
      return ble.readDescriptor(serviceId, characteristicId, uuid);
    },
    requestMTU: (mtu: number): Promise<number> => {
      return ble.requestMTU(mtu);
    },
    emitter: bleEmitter,
    eventType: {
      ON_CONNECTED: 'Connected',
      ON_DISCONNECTED: 'Disconnected',
      ON_SERVICESDISCOVERED: 'ServicesDiscovered',
      ON_READ_CHARACTERISTIC: 'CharacteristicRead',
      ON_CHANGE_CHARACTERISTIC: 'CharacteristicChanged',
      ON_READ_DESCRIPTOR: 'DescriptorRead',
      ON_FOUND: 'FoundBLEDevice',
      ...Platform.select({
        android: {
          ON_PERMIT_SCAN: 'PermitBLEScan',
          ON_PERMIT_ADVERTISE: 'PermitBLEAdvertise',
        },
        ios: {
          ON_POWER_ON: 'CBCentralManagerPowerOn',
          ON_POWER_OFF: 'CBCentralManagerPowerOff',
          ON_UNAUTHORIZED: 'CBCentralManagerUnauthorized',
        },
      }),
    },
    ...Platform.select({
      android: {
        BluetoothGattCharacteristic: {
          PROPERTY: {
            BROADCAST: BluetoothGattCharacteristic.PROPERTY.BROADCAST,
            EXTENDED_PROPS: BluetoothGattCharacteristic.PROPERTY.EXTENDED_PROPS,
            INDICATE: BluetoothGattCharacteristic.PROPERTY.INDICATE,
            NOTIFY: BluetoothGattCharacteristic.PROPERTY.NOTIFY,
            READ: BluetoothGattCharacteristic.PROPERTY.READ,
            SIGNED_WRITE: BluetoothGattCharacteristic.PROPERTY.SIGNED_WRITE,
            WRITE: BluetoothGattCharacteristic.PROPERTY.WRITE,
            WRITE_NO_RESPONSE:
              BluetoothGattCharacteristic.PROPERTY.WRITE_NO_RESPONSE,
          },
          PERMISSION: {
            READ: BluetoothGattCharacteristic.PERMISSION.READ,
            READ_ENCRYPTED:
              BluetoothGattCharacteristic.PERMISSION.READ_ENCRYPTED,
            READ_ENCRYPTED_MITM:
              BluetoothGattCharacteristic.PERMISSION.READ_ENCRYPTED_MITM,
            WRITE: BluetoothGattCharacteristic.PERMISSION.WRITE,
            WRITE_ENCRYPTED:
              BluetoothGattCharacteristic.PERMISSION.WRITE_ENCRYPTED,
            WRITE_ENCRYPTED_MITM:
              BluetoothGattCharacteristic.PERMISSION.WRITE_ENCRYPTED_MITM,
            WRITE_SIGNED: BluetoothGattCharacteristic.PERMISSION.WRITE_SIGNED,
            WRITE_SIGNED_MITM:
              BluetoothGattCharacteristic.PERMISSION.WRITE_SIGNED_MITM,
          },
        },
        BluetoothGattDescriptor: {
          PERMISSION: {
            READ: BluetoothGattDescriptor.PERMISSION.READ,
            READ_ENCRYPTED: BluetoothGattDescriptor.PERMISSION.READ_ENCRYPTED,
            READ_ENCRYPTED_MITM:
              BluetoothGattDescriptor.PERMISSION.READ_ENCRYPTED_MITM,
            WRITE: BluetoothGattDescriptor.PERMISSION.WRITE,
            WRITE_ENCRYPTED: BluetoothGattDescriptor.PERMISSION.WRITE_ENCRYPTED,
            WRITE_ENCRYPTED_MITM:
              BluetoothGattDescriptor.PERMISSION.WRITE_ENCRYPTED_MITM,
            WRITE_SIGNED: BluetoothGattDescriptor.PERMISSION.WRITE_SIGNED,
            WRITE_SIGNED_MITM:
              BluetoothGattDescriptor.PERMISSION.WRITE_SIGNED_MITM,
          },
        },
      },
      ios: {},
    }),
  },
  common: {
    name: common.getConstants().name as string,
    version: common.getConstants().version as string,
    buildVersion: common.getConstants().buildVersion as number,
    identifier: common.getConstants().identifier as string,
    setColorMode: function (colorMode: number): Promise<null> {
      if (Platform.OS === 'android') {
        return common.setColorMode(colorMode);
      }
      return Promise.resolve(null);
    },
    getColorMode: function (): Promise<number> {
      if (Platform.OS === 'android') {
        return common.getColorMode();
      }
      return Promise.resolve(0);
    },
    getKeyHashes: function (): Promise<string[]> {
      if (Platform.OS !== 'android') {
        return Promise.resolve([]);
      }
      return common.getKeyHashes();
    },
    canDrawOverlays: function (): boolean {
      if (Platform.OS === 'android') {
        return common.canDrawOverlays();
      } else {
        return true;
      }
    },
    reqOverlayPermissions: function (): Promise<boolean> {
      if (Platform.OS === 'android') {
        return common.reqOverlayPermissions();
      } else {
        return Promise.resolve(true);
      }
    },
    isIgnoringBatteryOptimizations: function (): boolean {
      if (Platform.OS === 'android') {
        return common.isIgnoringBatteryOptimizations();
      } else {
        return true;
      }
    },
    reqIgnoringBatteryOptimizations: function (): Promise<boolean> {
      if (Platform.OS === 'android') {
        return common.reqIgnoringBatteryOptimizations();
      } else {
        return Promise.resolve(true);
      }
    },
    checkLocalNetworking: function (): Promise<boolean> {
      if (Platform.OS === 'ios') {
        return common.checkLocalNetworking();
      } else {
        return Promise.resolve(true);
      }
    },
    navigateToSettings: async function () {
      if (Platform.OS === 'ios') {
        try {
          await common.navigateToSettings();
        } catch (err) {
          console.error(err);
        }
      }
    },
  },
  ...Platform.select({
    android: {
      COLOR_MODE: {
        DEFAULT: common.getConstants().COLOR_MODE.DEFAULT as number,
        WIDE_COLOR_GAMUT: common.getConstants().COLOR_MODE
          .WIDE_COLOR_GAMUT as number,
        HDR: common.getConstants().COLOR_MODE.HDR as number,
      },
    },
    ios: {},
  }),
  register: {
    getNearbyDevice: function (): void {},
  },
};

export default API;
