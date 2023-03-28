import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

type BluetoothAdvertisementData = {
  localName?: string;
  serviceUUIDs?: string;
};

export interface Spec extends TurboModule {
  checkPermission(): boolean;
  startAdvertising(advertisementData: BluetoothAdvertisementData): void;
  stopAdvertising(): void;
  startScan(): void;
  isScanning(): boolean;
  startScanByCompanyId(companyIds: number[]): void;
  stopScan(): void;
  connect(identifier: string): void;
  disconnect(): void;
  discoverServices(): Promise<string[]>;
  discoverCharacteristics(serviceId: string): Promise<string[]>;
  discoverDescriptors(characteristicId: string): Promise<string[]>;
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

  // RCTEventEmitter method
  addListener(eventName: string): void;
  removeListeners(count: number): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('BluetoothModule');
