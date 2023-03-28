import { NativeEventEmitter, Platform } from 'react-native';
import type Bluetooth from './types/Bluetooth';
import type Common from './types/Common';
import { getBluetoothModule } from './utils';

const BluetoothEventEmitter = new NativeEventEmitter(getBluetoothModule());

const BluetoothModule: Bluetooth = (() => {
  if (Platform.OS === 'android') {
    return require('./modules/Bluetooth.android').default;
  } else if (Platform.OS === 'ios') {
    return require('./modules/Bluetooth.ios').default;
  } else {
    return {};
  }
})();

const CommonModule: Common = (() => {
  if (Platform.OS === 'android') {
    return require('./modules/Common.android').default;
  } else if (Platform.OS === 'ios') {
    return require('./modules/Common.ios').default;
  } else {
    return {};
  }
})();

const StlApi = {
  ble: {
    ...BluetoothModule,
    emitter: BluetoothEventEmitter,
  },
  common: CommonModule,
  register: {
    getNearbyDevice: function (): void {},
  },
};

export default StlApi;
