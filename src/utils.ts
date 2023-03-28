import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-stl-api' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

export function checkModuleIsLinked(module: any) {
  return module
    ? false
    : new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      );
}

export function getBluetoothModule<T = any>(): T {
  const moduleName = 'BluetoothModule';
  const module: T = isTurboModuleEnabled
    ? require(`./NativeBluetoothModule`).default
    : NativeModules[moduleName];
  checkModuleIsLinked(module);
  return module;
}

export function getCommonModule<T = any>(): T {
  const moduleName = 'Common';
  const module: T = isTurboModuleEnabled
    ? require(`./NativeCommon`).default
    : NativeModules[moduleName];
  checkModuleIsLinked(module);
  return module;
}

// @ts-expect-error
export const isTurboModuleEnabled = global.__turboModuleProxy != null;
