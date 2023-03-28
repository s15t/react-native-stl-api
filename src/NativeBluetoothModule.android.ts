import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';
import type { BluetoothAdvertisementData } from './types/BluetoothAdvertisementData';

export interface Spec extends TurboModule {
  readonly getConstants: () => {
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
  };
  checkAdvertisePermission(): Promise<boolean>;
  requestAdvertisePermission(): Promise<void>;
  checkScanPermission(): Promise<boolean>;
  requestScanPermissions(): Promise<void>;
  startAdvertising(advertisementData: BluetoothAdvertisementData): void;
  stopAdvertising(): void;
  startScan(): Promise<void>;
  startScanByCompanyId(companyIds: number[]): Promise<void>;
  isDiscovering(): Promise<boolean>;
  stopScan(): Promise<void>;
  connect(identifier: string): void;
  disconnect(): void;
  discoverServices(): Promise<{
    serviceIds: string[];
    characteristics: string[];
    descriptors: string[];
  }>;
  writeCharacteristic(
    serviceId: string,
    uuid: string,
    data: string
  ): Promise<void>;
  readCharacteristic(serviceId: string, uuid: string): Promise<void>;
  writeDescriptor(
    serviceId: string,
    characteristicId: string,
    uuid: string,
    data: string
  ): Promise<void>;
  readDescriptor(
    serviceId: string,
    characteristicId: string,
    uuid: string
  ): Promise<void>;
  requestMTU(mtu: number): Promise<void>;

  // RCTEventEmitter method
  addListener(eventName: string): void;
  removeListeners(count: number): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('BluetoothModule');
