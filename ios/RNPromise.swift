//
//  RNPromise.swift
//  react-native-stl-api
//
//  Created by Seongwoo Park on 2023/03/24.
//

struct RNPromise {
    let resolver: RCTPromiseResolveBlock
    let rejecter: RCTPromiseRejectBlock
    
    init(_ resolver: @escaping RCTPromiseResolveBlock, _ rejecter: @escaping RCTPromiseRejectBlock) {
        self.resolver = resolver
        self.rejecter = rejecter
    }
}
