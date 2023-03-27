import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  getConstants: () => {
    readonly name: string;
    readonly version: string;
    readonly buildVersion: number;
    readonly identifier: string;
  };

  navigateToSettings(): Promise<void>;
  getKeyHashes(): Promise<string[]>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Common');
