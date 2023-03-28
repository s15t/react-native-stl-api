/**
 * Common union types
 *
 *
 */

type Common = {
  name: string;
  version: string;
  buildVersion: number;
  identifier: string;
  COLOR_MODE: {
    DEFAULT: string;
    WIDE_COLOR_GAMUT: string;
    HDR: string;
  };
  setColorMode: (colorMode: number) => Promise<void>;
  getColorMode: () => Promise<number>;
  canDrawOverlays: () => boolean;
  reqOverlayPermissions: () => Promise<boolean>;
  isIgnoringBatteryOptimizations: () => boolean;
  reqIgnoringBatteryOptimizations: () => Promise<boolean>;
  getKeyHashes: () => Promise<string[]>;
  navigateToSettings: () => Promise<void>;
};

export default Common;
