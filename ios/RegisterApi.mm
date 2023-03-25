#import "RegisterApi.h"

@implementation RegisterApi
RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup
{
    return false;
}

RCT_REMAP_METHOD(getNearbyDevice,
                 getNearbyDevice:(RCTPromiseResolveBlock)resolve
                 reject:(RCTPromiseRejectBlock)reject)
{
    resolve(@{
        @"type": @"",
        @"id": @""
    });
}

// Don't compile this code when we build for the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeRegisterApiSpecJSI>(params);
}
#endif

@end
