import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export type NearbyDeviceProps = {
  type: string;
  id: string;
};

export interface Spec extends TurboModule {
  getNearbyDevice(): Promise<NearbyDeviceProps>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('RegisterApi');
