#import "Common.h"

@implementation Common
{
}
RCT_EXPORT_MODULE()

- (instancetype)init
{
    if ((self = [super init])) {
        // lazy initializer
    }
    return self;
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

- (NSDictionary *)constantsToExport
{
    return @{
        @"name": [[NSBundle mainBundle].infoDictionary objectForKey:@"CFBundleName"],
        @"version": [[NSBundle mainBundle].infoDictionary objectForKey:@"CFBundleShortVersionString"],
        @"buildVersion": [[NSBundle mainBundle].infoDictionary objectForKey:@"CFBundleVersion"],
        @"identifier": [[NSBundle mainBundle].infoDictionary objectForKey:@"CFBundleIdentifier"]
    };
}

RCT_REMAP_METHOD(navigateToSettings,
                 navigateToSettings:(RCTPromiseResolveBlock)resolve
                 reject:(RCTPromiseRejectBlock)reject)
{
    NSURL * settingURL = [NSURL URLWithString:UIApplicationOpenSettingsURLString];
    if (settingURL) {
        if ([[UIApplication sharedApplication] canOpenURL:settingURL]) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[UIApplication sharedApplication] openURL:settingURL options:[NSMutableDictionary dictionary] completionHandler:^(BOOL success){
                    if (success) {
                        resolve(nil);
                    } else {
                        reject(@"NAVIGATE_TO_SETTINGS_FAILER", @"Cannot navigate to setting URL.", nil);
                    }
                }];
            });
        } else {
            reject(@"NAVIGATE_TO_SETTINGS_FAILER", @"Cannot navigate to setting URL.", nil);
        }
    } else {
        reject(@"NAVIGATE_TO_SETTINGS_FAILER", @"Cannot read setting URL.", nil);
    }
}

RCT_REMAP_METHOD(getKeyHashes,
                 getKeyHashes:(RCTPromiseResolveBlock)resolve
                 reject:(RCTPromiseRejectBlock)reject)
{
    resolve(@[]);
}

// Don't compile this code when we build for the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
- (facebook::react::ModuleConstants<JS::NativeCommon::Constants::Builder>)getConstants
{
    return [self constantsToExport];
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeCommonSpecJSI>(params);
}
#endif

@end
