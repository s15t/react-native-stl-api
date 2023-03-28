/**
 * Common Module Props
 *
 *
 */

import type { Spec } from '../NativeCommon.ios';
import { getModule } from '../utils';

const common = getModule<Spec>('Common');

export default {
  name: common.getConstants().name,
  version: common.getConstants().version,
  buildVersion: common.getConstants().buildVersion,
  identifier: common.getConstants().identifier,
  getKeyHashes: common.getKeyHashes,
  navigateToSettings: common.navigateToSettings,
};
