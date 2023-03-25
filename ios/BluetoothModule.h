#import <CoreBluetooth/CoreBluetooth.h>
#import <React/RCTEventEmitter.h>
#import <React/RCTLog.h>
#import "RNPromise.h"

#ifdef RCT_NEW_ARCH_ENABLED
#import "RNStlApiSpec.h"

@interface BluetoothModule : RCTEventEmitter <NativeBluetoothModuleSpec, CBCentralManagerDelegate, CBPeripheralManagerDelegate, CBPeripheralDelegate>
#else
#import <React/RCTBridgeModule.h>

@interface BluetoothModule : RCTEventEmitter <RCTBridgeModule, CBCentralManagerDelegate, CBPeripheralManagerDelegate, CBPeripheralDelegate>
#endif
{
    @private
    CBPeripheral *_currentPeripheral;
    NSMutableDictionary<id, CBPeripheral *> *_deviceMap;
    NSArray<NSNumber *> *_companyIds;
}

- (instancetype)init;

@property (nonatomic, strong) CBCentralManager *manager;

@property (nonatomic, strong) CBPeripheralManager *peripheralManager;

@property (nonatomic, strong) RNPromise *DiscoverServicesPromise;

@property (nonatomic, strong) RNPromise *DiscoverCharacteristicsPromise;

@property (nonatomic, strong) RNPromise *DiscoverDescriptorsPromise;

@property (nonatomic, strong) RNPromise *ReadCharacteristicPromise;

@property (nonatomic, strong) RNPromise *WriteCharacteristicPromise;

@property (nonatomic, strong) RNPromise *ReadDescriptorPromise;

@property (nonatomic, strong) RNPromise *WriteDescriptorPromise;

- (CBCharacteristic *)getCharacteristic:(NSString *)serviceId characteristicId:(NSString *)characteristicId;

- (CBDescriptor *)getDescriptor:(NSString *)serviceId characteristicId:(NSString *)characteristicId uuidString:(NSString *)uuid;

@end

