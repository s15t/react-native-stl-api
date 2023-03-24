//
//  BluetoothModule.m
//  react-native-stl-api
//
//  Created by Seongwoo Park on 2022/09/19.
//

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(BluetoothModule, NSObject)

RCT_EXTERN_METHOD(startScan)

RCT_EXTERN_METHOD(startAdvertising:(NSDictionary *)advertisementData)

RCT_EXTERN_METHOD(stopAdvertising)

RCT_EXTERN_METHOD(startScanByCompanyId:(NSNumber *)companyId)

RCT_EXTERN_METHOD(stopScan)

RCT_EXTERN_METHOD(connect:(NSString *)identifier)

RCT_EXTERN_METHOD(requestMtu:(RCTPromiseResolveBlock)resolve rejecter(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(discoverServices:(RCTPromiseResolveBlock)resolve rejecter(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(discoverCharacteristics:(NSString *)uuidString (RCTPromiseResolveBlock)resolve rejecter(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(discoverDescriptors:(NSString *)uuidString (RCTPromiseResolveBlock)resolve rejecter(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(writeCharacteristic:(NSString *)serviceId uuidString(NSString *)uuidString base64Encoded (NSString *)base64Encoded resolver(RCTPromiseResolveBlock)resolve rejecter(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(writeDescriptor:(NSString *)serviceId characteristicId(NSString *)characteristicId uuidString(NSString *)uuidString base64Encoded (NSString *)base64Encoded resolver(RCTPromiseResolveBlock)resolve rejecter(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(readCharacteristic:(NSString *)serviceId uuidString(NSString *)uuidString resolver(RCTPromiseResolveBlock)resolve rejecter(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(readDescriptor:(NSString *)serviceId characteristicId(NSString *)characteristicId uuidString(NSString *)uuidString resolver(RCTPromiseResolveBlock)resolve rejecter(RCTPromiseRejectBlock)reject)

RCT_EXTERN_METHOD(disconnect)

@end
