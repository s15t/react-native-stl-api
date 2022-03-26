//
//  common.swift
//  StlApi
//
//  Created by Seongwoo Park on 2022/03/27.
//  Copyright © 2022 Facebook. All rights reserved.
//

@objc(CommonAPIModule)
class CommonAPIModule: NSObject, RCTBridgeModule {
    
    @objc
    static func moduleName() -> String! {
        return "common"
    }
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
        return false
    }

    @objc(getKeyHashes:withRejecter:)
    func getKeyHashes(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) {
        resolve([])
    }
    
    @objc
    func constantsToExport() -> [AnyHashable: Any]! {
      return [
        "name": Bundle.main.infoDictionary!["CFBundleName"]!,
        "version": Bundle.main.infoDictionary!["CFBundleShortVersionString"]!,
        "buildVersion": String(format: "%X", (Bundle.main.infoDictionary!["CFBundleVersion"] as! NSString).integerValue),
        "identifier": Bundle.main.infoDictionary!["CFBundleIdentifier"]!,
      ]
    }
}
