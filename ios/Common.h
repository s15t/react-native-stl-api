#ifdef RCT_NEW_ARCH_ENABLED
#import "RNStlApiSpec.h"

@interface Common : NSObject <NativeCommonSpec>
#else
#import <React/RCTBridgeModule.h>

@interface Common : NSObject <RCTBridgeModule>
#endif

@end
