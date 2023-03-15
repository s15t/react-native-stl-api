//
//  common.swift
//  StlApi
//
//  Created by Seongwoo Park on 2022/03/27.
//  Copyright © 2022 Facebook. All rights reserved.
//

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(CommonAPIModule, NSObject)

RCT_EXTERN_METHOD(checkLocalNetworking:(RCTPromiseResolveBlock)resolver)

RCT_EXTERN_METHOD(navigateToSettings:(RCTPromiseResolveBlock)resolver
                  withRejecter:(RCTPromiseRejectBlock)rejecter)

RCT_EXTERN_METHOD(getKeyHashes:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject)

@end
