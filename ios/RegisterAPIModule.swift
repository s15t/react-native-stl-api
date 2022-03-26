//
//  register.swift
//  StlApi
//
//  Created by Seongwoo Park on 2022/03/27.
//  Copyright © 2022 Facebook. All rights reserved.
//

@objc(RegisterAPIModule)
class RegisterAPIModule: NSObject, RCTBridgeModule {
    
    @objc
    static func moduleName() -> String! {
        return "register"
    }
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
        return false
    }

    @objc(getNearbyDevice:withRejecter:)
    func getNearbyDevice(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        resolve([
            "type": "",
            "id": ""
        ])
    }
}
