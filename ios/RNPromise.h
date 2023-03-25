#import <React/RCTBridgeModule.h>

@interface RNPromise : NSObject

@property (nonatomic, copy) RCTPromiseResolveBlock resolve;

@property (nonatomic, copy) RCTPromiseRejectBlock reject;

- (instancetype) init:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;

@end
