import { NativeEventEmitter, NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-stl-api' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

const common = NativeModules.common
  ? NativeModules.common
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

const register = NativeModules.register
  ? NativeModules.register
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

const ble = NativeModules.ble
  ? NativeModules.ble
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );
const bleEmitter = new NativeEventEmitter(NativeModules.ble);

interface RegisterGetNearbyDevice {
  id: string;
  type: number;
}

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
    startScan: (): Promise<void> => ble.startScan,
    startScanByCompanyId: (companyId: number) => {
      ble.startScanByCompanyId(companyId);
    },
    stopScan: (): Promise<void> => ble.stopScan,
    isDiscovering: (): Promise<boolean> => {
      return ble.isDiscovering();
    },
    connect: (identifier: string) => ble.connect(identifier),
    disconnect: () => ble.disconnect,
    emitter: () => bleEmitter,
    eventType: {
      ON_READ_CHARACTERISTIC: 'CharacteristicRead',
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
  },
  common: {
    name: common.name as string,
    version: common.version as string,
    buildVersion: common.buildVersion as number,
    identifier: common.identifier as string,
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
  },
  COLOR_MODE: {
    DEFAULT: common.COLOR_MODE.DEFAULT as number,
    WIDE_COLOR_GAMUT: common.COLOR_MODE.WIDE_COLOR_GAMUT as number,
    HDR: common.COLOR_MODE.HDR as number,
  },
  register: {
    getNearbyDevice: function (): Promise<RegisterGetNearbyDevice> {
      return register.getNearbyDevice();
    },
  },
};

export default API;
