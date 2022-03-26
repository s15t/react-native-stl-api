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
    getKeyHashes: function (): Promise<string[]> {
      if (Platform.OS !== 'android') {
        return Promise.resolve([]);
      }
      return common.getKeyHashes();
    },
  },
  register: {
    getNearbyDevice: function (): Promise<RegisterGetNearbyDevice> {
      return register.getNearbyDevice();
    },
  },
};

export default API;
