/**
 * Bluetooth union types
 *
 *
 */

import type { NativeEventEmitter } from 'react-native';
import type { BluetoothAdvertisementData } from './BluetoothAdvertisementData';

type Bluetooth = {
  readonly BluetoothGattCharacteristic: {
    readonly PROPERTY: {
      readonly BROADCAST: number;
      readonly EXTENDED_PROPS: number;
      readonly INDICATE: number;
      readonly NOTIFY: number;
      readonly READ: number;
      readonly SIGNED_WRITE: number;
      readonly WRITE: number;
      readonly WRITE_NO_RESPONSE: number;
    };
    readonly PERMISSION: {
      readonly READ: number;
      readonly READ_ENCRYPTED: number;
      readonly READ_ENCRYPTED_MITM: number;
      readonly WRITE: number;
      readonly WRITE_ENCRYPTED: number;
      readonly WRITE_ENCRYPTED_MITM: number;
      readonly WRITE_SIGNED: number;
      readonly WRITE_SIGNED_MITM: number;
    };
  };
  readonly BluetoothGattDescriptor: {
    readonly PERMISSION: {
      readonly READ: number;
      readonly READ_ENCRYPTED: number;
      readonly READ_ENCRYPTED_MITM: number;
      readonly WRITE: number;
      readonly WRITE_ENCRYPTED: number;
      readonly WRITE_ENCRYPTED_MITM: number;
      readonly WRITE_SIGNED: number;
      readonly WRITE_SIGNED_MITM: number;
    };
  };
  checkAdvertisePermission: () => Promise<boolean>;
  requestAdvertisePermission: () => Promise<void>;
  checkScanPermission: () => Promise<boolean>;
  requestScanPermissions: () => Promise<void>;
  checkPermission: () => boolean;
  startAdvertising: (advertisementData: BluetoothAdvertisementData) => void;
  stopAdvertising: () => void;
  startScan: () => Promise<void> | void;
  startScanByCompanyId: (companyIds: number[]) => Promise<void> | void;
  isScanning: () => boolean;
  isDiscovering: () => Promise<boolean>;
  stopScan: () => Promise<void>;
  connect: (identifier: string) => void;
  disconnect: () => void;
  discoverServices: () =>
    | Promise<{
        serviceIds: string[];
        characteristics: string[];
        descriptors: string[];
      }>
    | Promise<string[]>;
  discoverCharacteristics: (serviceId: string) => Promise<string[]>;
  discoverDescriptors: (characteristicId: string) => Promise<string[]>;
  writeCharacteristic: (
    serviceId: string,
    uuid: string,
    data: string
  ) => Promise<void>;
  readCharacteristic: (
    serviceId: string,
    uuid: string
  ) => Promise<{ uuid: string; data: string }>;
  writeDescriptor: (
    serviceId: string,
    characteristicId: string,
    uuid: string,
    data: string
  ) => Promise<void>;
  readDescriptor: (
    serviceId: string,
    characteristicId: string,
    uuid: string
  ) => Promise<{ uuid: string; data: string }>;
  requestMTU: (mtu: number) => Promise<void>;
  emitter: typeof NativeEventEmitter;
  eventType: {
    ON_CONNECTED: string;
    ON_DISCONNECTED: string;
    ON_SERVICESDISCOVERED: string;
    ON_READ_CHARACTERISTIC: string;
    ON_CHANGE_CHARACTERISTIC: string;
    ON_READ_DESCRIPTOR: string;
    ON_FOUND: string;
    ON_PERMIT_SCAN: string;
    ON_PERMIT_ADVERTISE: string;
    ON_POWER_ON: string;
    ON_POWER_OFF: string;
    ON_UNAUTHORIZED: string;
  };
};

export default Bluetooth;
