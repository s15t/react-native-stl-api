import { Platform } from 'react-native';
import type BluetoothAndroid from './modules/Bluetooth.android';
import type BluetoothIOS from './modules/Bluetooth.ios';
import type CommonAndroid from './modules/Common.android';
import type CommonIOS from './modules/Common.ios';

interface StlApi {
  ble: typeof BluetoothAndroid | typeof BluetoothIOS;
  common: typeof CommonAndroid | typeof CommonIOS;
  register: {
    getNearbyDevice: () => void;
  };
}

const StlApi: StlApi = {
  ble: Platform.select({
    android: require('./modules/Bluetooth.android'),
    ios: require('./modules/Bluetooth.ios'),
  }),
  common: Platform.select({
    android: require('./modules/Common.android'),
    ios: require('./modules/Common.ios'),
  }),
  register: {
    getNearbyDevice: function (): void {},
  },
};

export default StlApi;
