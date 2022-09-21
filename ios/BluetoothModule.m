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

RCT_EXTERN_METHOD(writeCharacteristic:(NSString *)uuidString base64Encoded (NSString *)base64Encoded)

RCT_EXTERN_METHOD(writeDescriptor:(NSString *)uuidString base64Encoded (NSString *)base64Encoded)

RCT_EXTERN_METHOD(readCharacteristic:(NSString *)uuidString)

RCT_EXTERN_METHOD(readDescriptor:(NSString *)uuidString)

RCT_EXTERN_METHOD(disconnect)

@end
