//
//  register.m
//  StlApi
//
//  Created by Seongwoo Park on 2022/03/27.
//  Copyright © 2022 Facebook. All rights reserved.
//

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(RegisterAPIModule, NSObject)

RCT_EXTERN_METHOD(getNearbyDevice:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

@end
