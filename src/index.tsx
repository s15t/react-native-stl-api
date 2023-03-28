// @ts-expect-error
import CommonModule from './modules/Common';
// @ts-expect-error
import BluetoothModule from './modules/Bluetooth';

const API = {
  ble: BluetoothModule,
  common: CommonModule,
  register: {
    getNearbyDevice: function (): void {},
  },
};

export default API;
