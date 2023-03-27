import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  getConstants: () => {
    readonly name: string;
    readonly version: string;
    readonly buildVersion: number;
    readonly identifier: string;
    readonly COLOR_MODE: {
      readonly DEFAULT: string;
      readonly WIDE_COLOR_GAMUT: string;
      readonly HDR: string;
    };
  };
  setColorMode(colorMode: number): Promise<void>;
  getColorMode(): Promise<number>;
  canDrawOverlays(): boolean;
  reqOverlayPermissions(): Promise<boolean>;
  isIgnoringBatteryOptimizations(): boolean;
  reqIgnoringBatteryOptimizations(): Promise<boolean>;
  getKeyHashes(): Promise<string[]>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Common');
