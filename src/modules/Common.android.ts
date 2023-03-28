/**
 * Common android module
 *
 *
 */

import type { Spec } from '../NativeCommon.android';
import { getCommonModule } from '../utils';

const common = getCommonModule<Spec>();

export default {
  name: common.getConstants().name,
  version: common.getConstants().version,
  buildVersion: common.getConstants().buildVersion,
  identifier: common.getConstants().identifier,
  COLOR_MODE: common.getConstants().COLOR_MODE,
  setColorMode: common.setColorMode,
  getColorMode: common.getColorMode,
  canDrawOverlays: common.canDrawOverlays,
  reqOverlayPermissions: common.reqOverlayPermissions,
  isIgnoringBatteryOptimizations: common.isIgnoringBatteryOptimizations,
  reqIgnoringBatteryOptimizations: common.reqIgnoringBatteryOptimizations,
  getKeyHashes: common.getKeyHashes,
};
