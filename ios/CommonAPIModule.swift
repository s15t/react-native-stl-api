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
    
    @objc(checkLocalNetworking:)
    func checkLocalNetworking(resolver: RCTPromiseResolveBlock) {
        resolver(UserDefaults.standard.bool(forKey: "NSAllowsLocalNetworking"))
    }
    
    @objc(navigateToSettings:rejecter:)
    func navigateToSettings(resolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) {
        if let settingURL = URL(string: UIApplication.openSettingsURLString) {
            if UIApplication.shared.canOpenURL(settingURL) {
                UIApplication.shared.open(settingURL, options: [:])
                resolver(nil)
            } else {
                rejecter("NAVIGATE_TO_SETTINGS_FAILER", "Cannot navigate to setting URL.", nil)
            }
        } else {
            rejecter("NAVIGATE_TO_SETTINGS_FAILER", "Cannot read setting URL.", nil)
        }
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
