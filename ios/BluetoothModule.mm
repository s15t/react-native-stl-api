#import "BluetoothModule.h"

#ifdef RCT_NEW_ARCH_ENABLED
typedef JS::NativeBluetoothModule::BluetoothAdvertisementData & AdvertisementData;
#else
typedef NSDictionary * AdvertisementData;
#endif

@implementation BluetoothModule
{
    bool hasListeners;
}
RCT_EXPORT_MODULE()

- (instancetype)init
{
    if ((self = [super init])) {
        _manager = [[CBCentralManager alloc] initWithDelegate:self queue:dispatch_get_main_queue()];
        _peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:self queue:dispatch_get_main_queue()];
        _isPermission = false;
    }
    return self;
}

+ (BOOL)requiresMainQueueSetup
{
  return YES;
}

- (void)startObserving
{
    hasListeners = YES;
}

- (void)stopObserving
{
    hasListeners = NO;
}

- (void)emitWithName:(NSString *)eventName body:(id)body
{
    if (hasListeners) {
        [self sendEventWithName:eventName body:body];
    }
}

- (NSArray<NSString *> *)supportedEvents {
    return @[
        @"CBManagerPowerOn",
        @"CBManagerPowerOff",
        @"CBManagerUnauthorized",
        @"CBPeripheralManagerPowerOn",
        @"CBPeripheralManagerPowerOff",
        @"CBPeripheralManagerUnauthorized",
        @"CharacteristicRead",
        @"DescriptorRead",
        @"FoundBLEDevice",
        @"Connected",
        @"Disconnected"
    ];
}

RCT_REMAP_BLOCKING_SYNCHRONOUS_METHOD(checkPermission,
                                      NSNumber *, checkPermission)
{
    return [NSNumber numberWithBool:_isPermission];
}

RCT_REMAP_METHOD(startAdvertising,
                 startAdvertising)
{
    if (!_peripheralManager.isAdvertising) {
        RCTLogInfo(@"start advertising without options");
        [_peripheralManager startAdvertising:nil];
    }
}

RCT_REMAP_METHOD(startAdvertising,
                 startAdvertising:(AdvertisementData)advertisementData)
{
    if (!_peripheralManager.isAdvertising) {
#ifdef RCT_NEW_ARCH_ENABLED
        NSString *localName = advertisementData.localName();
        NSString *serviceUUIDs = advertisementData.serviceUUIDs();
#else
        NSString *localName = advertisementData[@"localName"];
        NSString *serviceUUIDs = advertisementData[@"serviceUUIDs"];
#endif
        if (localName != nil || serviceUUIDs != nil) {
            [_peripheralManager startAdvertising:@{
                CBAdvertisementDataLocalNameKey: localName ? : @"",
                CBAdvertisementDataServiceUUIDsKey: serviceUUIDs ? : @""
            }];
        } else {
            RCTLogInfo(@"start advertising without options");
            [_peripheralManager startAdvertising:nil];
        }
    }
}

RCT_REMAP_BLOCKING_SYNCHRONOUS_METHOD(isAdvertising,
                                      BOOL, isAdvertising)
{
    return _peripheralManager.isAdvertising;
}

RCT_REMAP_METHOD(stopAdvertising,
                 stopAdvertising)
{
    if (_peripheralManager.isAdvertising) {
        [_peripheralManager stopAdvertising];
    }
}

RCT_REMAP_METHOD(startScan,
                 startScan)
{
    if (!_manager.isScanning) {
        _companyIds = [[NSArray alloc] init];
        [_manager scanForPeripheralsWithServices:nil options:nil];
        _deviceMap = [NSMutableDictionary dictionary];
    }
}

RCT_REMAP_METHOD(startScanByCompanyId,
                 startScanByCompanyId:(NSArray<NSNumber *> *)companyIds)
{
    if (!_manager.isScanning) {
        _companyIds = companyIds;
        [_manager scanForPeripheralsWithServices:nil options:nil];
        _deviceMap = [NSMutableDictionary dictionary];
    }
}

RCT_REMAP_BLOCKING_SYNCHRONOUS_METHOD(isScanning,
                                      NSNumber *, isScanning)
{
    return [NSNumber numberWithBool:_manager.isScanning];
}

RCT_REMAP_METHOD(stopScan,
                 stopScan)
{
    if (_manager.isScanning) {
        [_manager stopScan];
        _companyIds = [[NSArray alloc] init];
        _deviceMap = [NSMutableDictionary dictionary];
    }
}

RCT_REMAP_METHOD(connect,
                 connect:(NSString *)identifier)
{
    CBPeripheral *peripheral = [_deviceMap objectForKey:identifier];
    if (peripheral) {
        [_manager connectPeripheral:peripheral options:nil];
    }
}

RCT_REMAP_METHOD(disconnect,
                 disconnect)
{
    if (_currentPeripheral) {
        [_manager cancelPeripheralConnection:_currentPeripheral];
    }
}

RCT_REMAP_METHOD(discoverServices,
                 discoverServices:(RCTPromiseResolveBlock)resolve
                 reject:(RCTPromiseRejectBlock)reject)
{
    if (!_currentPeripheral || _currentPeripheral.services) {
        return;
    }

    _DiscoverServicesPromise = [[RNPromise alloc] init:resolve rejecter:reject];
    [_currentPeripheral discoverServices:nil];
}

RCT_REMAP_METHOD(discoverCharacteristics,
                 discoverCharacteristics:(NSString *)uuidString
                 resolve:(RCTPromiseResolveBlock)resolve
                 reject:(RCTPromiseRejectBlock)reject)
{
    if (!_currentPeripheral || !_currentPeripheral.services) {
        return;
    }

    for (CBService *service in _currentPeripheral.services) {
        if ([[service UUID] UUIDString] == uuidString) {
            _DiscoverCharacteristicsPromise = [[RNPromise alloc] init:resolve rejecter:reject];
            [_currentPeripheral discoverCharacteristics:nil forService:service];
            break;
        }
    }
}

RCT_REMAP_METHOD(discoverDescriptors,
                 discoverDescriptors:(NSString *)uuidString
                 resolve:(RCTPromiseResolveBlock)resolve
                 reject:(RCTPromiseRejectBlock)reject)
{
    if (!_currentPeripheral || !_currentPeripheral.services) {
        return;
    }

    for (CBService *service in _currentPeripheral.services) {
        CBCharacteristic *characteristic = [self getCharacteristic:service.UUID.UUIDString characteristicId:uuidString];
        if (characteristic) {
            _DiscoverDescriptorsPromise = [[RNPromise alloc] init:resolve rejecter:reject];
            [_currentPeripheral discoverDescriptorsForCharacteristic:characteristic];
            break;
        }
    }
}

RCT_REMAP_METHOD(writeCharacteristic,
                 writeCharacteristic:(NSString *)serviceId
                 uuid:(NSString *)uuidString
                 data:(NSString *)base64Encoded
                 resolve:(RCTPromiseResolveBlock)resolve
                 reject:(RCTPromiseRejectBlock)reject)
{
    NSData *data = [[NSData alloc] initWithBase64EncodedString:base64Encoded options:NSDataBase64DecodingIgnoreUnknownCharacters];
    if (!_currentPeripheral || !data) {
        return;
    }

    CBCharacteristic *characteristic = [self getCharacteristic:serviceId characteristicId:uuidString];
    if (characteristic) {
        _WriteCharacteristicPromise = [[RNPromise alloc] init:resolve rejecter:reject];
        [_currentPeripheral writeValue:data forCharacteristic:characteristic type:CBCharacteristicWriteWithoutResponse];
    }
}

RCT_REMAP_METHOD(writeDescriptor,
                 writeDescriptor:(NSString *)serviceId
                 characteristicId:(NSString *)characteristicId
                 uuid:(NSString *)uuidString
                 data:(NSString *)base64Encoded
                 resolve:(RCTPromiseResolveBlock)resolve
                 reject:(RCTPromiseRejectBlock)reject)
{
    NSData *data = [[NSData alloc] initWithBase64EncodedString:base64Encoded options:NSDataBase64DecodingIgnoreUnknownCharacters];
    if (!_currentPeripheral || !data) {
        return;
    }

    CBDescriptor *descriptor = [self getDescriptor:serviceId characteristicId:characteristicId uuidString:uuidString];
    if (descriptor) {
        _WriteDescriptorPromise = [[RNPromise alloc] init:resolve rejecter:reject];
        [_currentPeripheral writeValue:data forDescriptor:descriptor];
    }
}

RCT_REMAP_METHOD(readCharacteristic,
                 readCharacteristic:(NSString *)serviceId
                 uuid:(NSString *)uuidString
                 resolve:(RCTPromiseResolveBlock)resolve
                 reject:(RCTPromiseRejectBlock)reject)
{
    if (!_currentPeripheral) {
        return;
    }

    CBCharacteristic *characteristic = [self getCharacteristic:serviceId characteristicId:uuidString];
    if (characteristic) {
        _ReadCharacteristicPromise = [[RNPromise alloc] init:resolve rejecter:reject];
        [_currentPeripheral readValueForCharacteristic:characteristic];
    }
}

RCT_REMAP_METHOD(readDescriptor,
                 readDescriptor:(NSString *)serviceId
                 characteristicId:(NSString *)characteristicId
                 uuid:(NSString *)uuidString
                 resolve:(RCTPromiseResolveBlock)resolve
                 reject:(RCTPromiseRejectBlock)reject)
{
    if (!_currentPeripheral) {
        return;
    }

    CBDescriptor *descriptor = [self getDescriptor:serviceId characteristicId:characteristicId uuidString:uuidString];
    if (descriptor) {
        _ReadDescriptorPromise = [[RNPromise alloc] init:resolve rejecter:reject];
        [_currentPeripheral readValueForDescriptor:descriptor];
    }
}

- (CBCharacteristic *)getCharacteristic:(NSString *)serviceId characteristicId:(NSString *)characteristicId
{
    if (!_currentPeripheral || !_currentPeripheral.services) {
        return nil;
    }

    for (CBService *service in _currentPeripheral.services) {
        if (service.UUID.UUIDString != serviceId) {
            continue;
        }

        for (CBCharacteristic * characteristic in service.characteristics) {
            if (characteristic.UUID.UUIDString == characteristicId) {
                return characteristic;
            }
        }
    }
    return nil;
}

- (CBDescriptor *)getDescriptor:(NSString *)serviceId characteristicId:(NSString *)characteristicId uuidString:(NSString *)uuid
{
    CBCharacteristic *characteristic = [self getCharacteristic:serviceId characteristicId:characteristicId];
    if (!characteristic || !characteristic.descriptors) {
        return nil;
    }

    for (CBDescriptor *descriptor in characteristic.descriptors) {
        if (descriptor.UUID.UUIDString == uuid) {
            return descriptor;
        }
    }
    return nil;
}

// Don't compile this code when we build for the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeBluetoothModuleSpecJSI>(params);
}
#endif

//MARK: CBCentralManagerDelegate event
- (void)centralManagerDidUpdateState:(CBCentralManager *)central
{
    switch (central.state) {
        case CBManagerStatePoweredOn:
            [self emitWithName:@"CBManagerPowerOn" body:nil];
            _isPermission = true;
            break;
        case CBManagerStatePoweredOff:
            [self emitWithName:@"CBManagerPowerOff" body:nil];
            _isPermission = true;
            break;
        case CBManagerStateUnauthorized:
            [self emitWithName:@"CBManagerUnauthorized" body:nil];
            _isPermission = false;
            break;
        default:
            break;
    }
}

- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral
{
    peripheral.delegate = self;
    _currentPeripheral = peripheral;
    [self emitWithName:@"Connected" body:nil];
}

- (void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error
{
    peripheral.delegate = nil;
    _currentPeripheral = nil;
    [self emitWithName:@"Disconnected" body:nil];
}

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error
{
    peripheral.delegate = nil;
    _currentPeripheral = nil;
    [self emitWithName:@"Disconnected" body:nil];
}

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary<NSString *,id> *)advertisementData RSSI:(NSNumber *)RSSI
{
    NSString *identifier = [[peripheral identifier] UUIDString];
    NSString *localName = advertisementData[CBAdvertisementDataLocalNameKey] ?: @"Unknown Device";
    NSNumber *txPower = advertisementData[CBAdvertisementDataTxPowerLevelKey] ?: [[NSNumber alloc] initWithInt:0];
    NSData *manufacturerData = advertisementData[CBAdvertisementDataManufacturerDataKey];
    NSMutableDictionary *payload = [NSMutableDictionary dictionary];
    payload[@"identifier"] = identifier;
    payload[@"name"] = localName;
    payload[@"TxPowerLevel"] = txPower;
    payload[@"RSSI"] = RSSI;
    payload[@"ManufacturerSpecificData"] = nil;
    
    if (manufacturerData) {
        uint8_t *buffer = (uint8_t *)manufacturerData.bytes;
        UInt16 manufacturerId = buffer[0] + (buffer[1] << 8);
        NSData *specificData = [NSData dataWithBytes:(buffer + 2) length:manufacturerData.length - 2];
        payload[@"ManufacturerId"] = [[NSNumber alloc] initWithUnsignedShort:manufacturerId];
        if (_companyIds.count > 0) {
            for (NSNumber *companyId in _companyIds) {
                if ([companyId unsignedShortValue] == manufacturerId) {
                    if (specificData.length > 0) {
                        payload[@"ManufacturerSpecificData"] = [specificData base64EncodedStringWithOptions:0];
                    }
                }
            }
        } else {
            if (specificData.length > 0) {
                payload[@"ManufacturerSpecificData"] = [specificData base64EncodedStringWithOptions:0];
            }
        }
    }
    _deviceMap[identifier] = peripheral;
    [self emitWithName:@"FoundBLEDevice" body:payload];
}

//MARK: CBPeripheralManagerDelegate event
- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral
{
    switch (peripheral.state) {
        case CBManagerStatePoweredOn:
            [self emitWithName:@"CBPeripheralManagerPowerOn" body:nil];
            break;
        case CBManagerStatePoweredOff:
            [self emitWithName:@"CBPeripheralManagerPowerOff" body:nil];
            break;
        case CBManagerStateUnauthorized:
            [self emitWithName:@"CBPeripheralManagerUnauthorized" body:nil];
            break;
        default:
            break;
    }
}

//MARK: CBPeripheralDelegate event
- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error
{
    if (!_DiscoverServicesPromise) {
        return;
    }

    if (error) {
        _DiscoverServicesPromise.reject(@"E_GATT_ERROR", @"Cannot be found service(s).", error);
    } else {
        NSMutableArray<NSString *> *payload = [NSMutableArray array];
        for (CBService *service in peripheral.services) {
            [payload addObject:service.UUID.UUIDString];
        }
        _DiscoverServicesPromise.resolve(payload);
    }
    _DiscoverServicesPromise = nil;
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error
{
    if (!_DiscoverCharacteristicsPromise) {
        return;
    }

    if (error) {
        _DiscoverCharacteristicsPromise.reject(@"E_GATT_ERROR", @"Cannot be found characteristic(s).", error);
    } else {
        NSMutableArray<NSString *> *payload = [NSMutableArray array];
        for (CBCharacteristic *characteristic in service.characteristics) {
            [payload addObject:characteristic.UUID.UUIDString];
        }
        _DiscoverCharacteristicsPromise.resolve(payload);
    }
    _DiscoverCharacteristicsPromise = nil;
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverDescriptorsForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    if (!_DiscoverDescriptorsPromise) {
        return;
    }

    if (error) {
        _DiscoverDescriptorsPromise.reject(@"E_GATT_ERROR", @"Cannot be found descriptor(s).", error);
    } else {
        NSMutableArray<NSString *> *payload = [NSMutableArray array];
        for (CBDescriptor *descriptor in characteristic.descriptors) {
            [payload addObject:descriptor.UUID.UUIDString];
        }
        _DiscoverDescriptorsPromise.resolve(payload);
    }
    _DiscoverDescriptorsPromise = nil;
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    if (!_ReadCharacteristicPromise) {
        return;
    }

    if (error) {
        _ReadCharacteristicPromise.reject(@"E_GATT_ERROR", @"Cannot be read characteristic.", error);
    } else {
        NSMutableDictionary *payload = [NSMutableDictionary dictionary];
        [payload setObject:characteristic.UUID.UUIDString forKey:@"uuid"];
        if (characteristic.value) {
            [payload setObject:[characteristic.value base64EncodedStringWithOptions:0] forKey:@"data"];
        } else {
            [payload setValue:nil forKey:@"data"];
        }
        _ReadCharacteristicPromise.resolve(payload);
    }
    _ReadCharacteristicPromise = nil;
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForDescriptor:(CBDescriptor *)descriptor error:(NSError *)error
{
    if (!_ReadDescriptorPromise) {
        return;
    }

    if (error) {
        _ReadDescriptorPromise.reject(@"E_GATT_ERROR", @"Cannot be read descriptor.", error);
    } else {
        NSData *data = (NSData *)descriptor.value;

        NSMutableDictionary *payload = [NSMutableDictionary dictionary];
        [payload setObject:descriptor.UUID.UUIDString forKey:@"uuid"];
        if (data) {
            [payload setObject:[data base64EncodedStringWithOptions:0] forKey:@"data"];
        } else {
            [payload setValue:nil forKey:@"data"];
        }
        _ReadDescriptorPromise.resolve(payload);
    }
    _ReadDescriptorPromise = nil;
}

@end
