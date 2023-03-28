/**
 * Bluetooth iOS module
 *
 *
 */

import { NativeEventEmitter } from 'react-native';
import type { Spec } from '../NativeBluetoothModule.ios';
import BluetoothEvent from '../types/BluetoothEvent';
import { getModule } from '../utils';

const Bluetooth = getModule<Spec>('BluetoothModule');
const emitter = new NativeEventEmitter(Bluetooth);

export default {
  startAdvertising: Bluetooth.startAdvertising,
  stopAdvertising: Bluetooth.stopAdvertising,
  startScan: Bluetooth.startScan,
  isScanning: Bluetooth.isScanning,
  startScanByCompanyId: Bluetooth.startScanByCompanyId,
  stopScan: Bluetooth.stopScan,
  connect: Bluetooth.connect,
  disconnect: Bluetooth.disconnect,
  discoverServices: Bluetooth.discoverServices,
  discoverCharacteristics: Bluetooth.discoverCharacteristics,
  discoverDescriptors: Bluetooth.discoverDescriptors,
  writeCharacteristic: Bluetooth.writeCharacteristic,
  readCharacteristic: Bluetooth.readCharacteristic,
  writeDescriptor: Bluetooth.writeDescriptor,
  readDescriptor: Bluetooth.readDescriptor,
  emitter,
  eventType: {
    ...BluetoothEvent,
    ON_POWER_ON: 'CBCentralManagerPowerOn',
    ON_POWER_OFF: 'CBCentralManagerPowerOff',
    ON_UNAUTHORIZED: 'CBCentralManagerUnauthorized',
  },
};
