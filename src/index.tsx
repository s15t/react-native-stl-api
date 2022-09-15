import { NativeModules, Platform } from 'react-native';

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

interface RegisterGetNearbyDevice {
  id: string;
  type: number;
}

const API = {
  common: {
    name: common.name as string,
    version: common.version as string,
    buildVersion: common.buildVersion as number,
    identifier: common.identifier as string,
    setWideColorGamut: function (colorMode: number): Promise<null> {
      if (Platform.OS === 'android') {
        return common.setWideColorGamut(colorMode);
      }
      return Promise.resolve(null);
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
