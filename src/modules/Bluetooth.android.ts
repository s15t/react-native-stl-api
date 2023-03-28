/**
 * Bluetooth android module
 *
 *
 */
import type { Spec } from '../NativeBluetoothModule.android';
import { getModule } from '../utils';
import { NativeEventEmitter } from 'react-native';
import BluetoothEvent from '../types/BluetoothEvent';

const Bluetooth = getModule<Spec>('BluetoothModule');
const emitter = new NativeEventEmitter(Bluetooth);

export default {
  BluetoothGattCharacteristic:
    Bluetooth.getConstants().BluetoothGattCharacteristic,
  BluetoothGattDescriptor: Bluetooth.getConstants().BluetoothGattDescriptor,
  checkAdvertisePermission: Bluetooth.checkAdvertisePermission,
  requestAdvertisePermission: Bluetooth.requestAdvertisePermission,
  checkScanPermission: Bluetooth.checkScanPermission,
  requestScanPermissions: Bluetooth.requestScanPermissions,
  startAdvertising: Bluetooth.startAdvertising,
  stopAdvertising: Bluetooth.stopAdvertising,
  startScan: Bluetooth.startScan,
  startScanByCompanyId: Bluetooth.startScanByCompanyId,
  isDiscovering: Bluetooth.isDiscovering,
  stopScan: Bluetooth.stopScan,
  connect: Bluetooth.connect,
  disconnect: Bluetooth.disconnect,
  discoverServices: Bluetooth.discoverServices,
  writeCharacteristic: Bluetooth.writeCharacteristic,
  readCharacteristic: Bluetooth.readCharacteristic,
  writeDescriptor: Bluetooth.writeDescriptor,
  readDescriptor: Bluetooth.readDescriptor,
  requestMTU: Bluetooth.requestMTU,
  emitter,
  eventType: {
    ...BluetoothEvent,
    ON_PERMIT_SCAN: 'PermitBLEScan',
    ON_PERMIT_ADVERTISE: 'PermitBLEAdvertise',
  },
};
