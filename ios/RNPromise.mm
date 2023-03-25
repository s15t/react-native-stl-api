#import "RNPromise.h"

@implementation RNPromise

- (instancetype)init:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject
{
    if ((self = [super init])) {
        _resolve = resolve;
        _reject = reject;
    }
    return self;
}

@end
