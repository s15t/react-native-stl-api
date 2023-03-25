#ifdef RCT_NEW_ARCH_ENABLED
#import "RNStlApiSpec.h"

@interface RegisterApi : NSObject <NativeRegisterApiSpec>
#else
#import <React/RCTBridgeModule.h>

@interface RegisterApi : NSObject <RCTBridgeModule>
#endif

@end
