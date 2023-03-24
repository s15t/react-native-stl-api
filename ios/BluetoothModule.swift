//
//  BluetoothModule.swift
//  react-native-stl-api
//
//  Created by Seongwoo Park on 2022/09/19.
//

import CoreBluetooth

@objc(BluetoothModule)
class BluetoothModule: RCTEventEmitter, CBCentralManagerDelegate, CBPeripheralManagerDelegate, CBPeripheralDelegate {
    
    private var _currentPeripheral: CBPeripheral?
    private var _deviceMap: [AnyHashable: CBPeripheral]!
    private var _companyIds: [NSNumber]?
    private lazy var _manager = CBCentralManager(delegate: self, queue: DispatchQueue.main)
    private lazy var _peripheralManager = CBPeripheralManager(delegate: self, queue: DispatchQueue.main)
    
    private var mDiscoverServicesPromise: RNPromise?
    private var mDiscoverCharacteristicsPromise: RNPromise?
    private var mDiscoverDescriptorsPromise: RNPromise?
    private var mReadCharacteristicPromise: RNPromise?
    private var mWriteCharacteristicPromise: RNPromise?
    private var mReadDescriptorPromise: RNPromise?
    private var mWriteDescriptorPromise: RNPromise?
    
    override class func moduleName() -> String! {
        return "ble"
    }
    
    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    override func supportedEvents() -> [String]! {
        return [
            "CBCentralManagerPowerOn",
            "CBCentralManagerPowerOff",
            "CBCentralManagerUnauthorized",
            "CBPeripheralManagerPowerOn",
            "CBPeripheralManagerPowerOff",
            "CBPeripheralManagerUnauthorized",
            "CharacteristicRead",
            "DescriptorRead",
            "FoundBLEDevice",
            "Connected",
            "Disconnected"
        ]
    }
    
    @objc(startAdvertising:)
    func startAdvertising(advertisementData: [AnyHashable: Any]?) {
        if let advertisementData = advertisementData {
            _peripheralManager.startAdvertising([
                CBAdvertisementDataLocalNameKey: advertisementData["localName"] as? String ?? "",
                CBAdvertisementDataServiceUUIDsKey: advertisementData["serviceUUIDs"] as? String ?? ""
            ])
        } else {
            _peripheralManager.startAdvertising(nil)
        }
    }
    
    @objc(stopAdvertising)
    func stopAdvertising() {
        _peripheralManager.stopAdvertising()
    }
    
    @objc(startScan)
    func startScan() {
        if !_manager.isScanning {
            _manager.scanForPeripherals(withServices: nil)
            _deviceMap = [:]
        }
    }
    
    @objc(startScanByCompanyId:)
    func startScanByCompanyId(_ companyIds: [NSNumber]) {
        if !_manager.isScanning {
            _companyIds = companyIds
            _manager.scanForPeripherals(withServices: nil)
        }
    }
    
    @objc(stopScan)
    func stopScan() {
        if _manager.isScanning {
            _manager.stopScan()
            _deviceMap = [:]
        }
    }
    
    @objc(connect:)
    func connect(_ identifier: String) {
        if let index = _deviceMap.index(forKey: identifier)  {
            _manager.connect(_deviceMap[index].value)
        }
    }
    
    @objc(disconnect)
    func disconnect() {
        if _currentPeripheral != nil {
            _manager.cancelPeripheralConnection(_currentPeripheral!)
        }
    }
    
    @objc(requestMtu:rejecter:)
    func requestMtu(_ resolver: RCTPromiseResolveBlock, rejecter: RCTPromiseRejectBlock) {
        if let peripheral = _currentPeripheral {
            let mtu = peripheral.maximumWriteValueLength(for: .withoutResponse)
            print("Bluetooth MTU: \(mtu)")
            resolver(nil)
        }
    }
    
    @objc(discoverServices:rejecter:)
    func discoverServices(_ resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        if let peripheral = _currentPeripheral {
            mDiscoverServicesPromise = RNPromise(resolver, rejecter)
            peripheral.discoverServices(nil)
        }
    }
    
    @objc(discoverCharacteristics:resolver:rejecter:)
    func discoverCharacteristics(_ uuidString: String, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        if let peripheral = _currentPeripheral {
            if let services = peripheral.services {
                for service in services {
                    if service.uuid.uuidString == uuidString {
                        mDiscoverCharacteristicsPromise = RNPromise(resolver, rejecter)
                        peripheral.discoverCharacteristics(nil, for: service)
                        break
                    }
                }
            }
        }
    }
    
    @objc(discoverDescriptors:resolver:rejecter:)
    func discoverDescriptors(_ uuidString: String, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        if let peripheral = _currentPeripheral {
            if let services = peripheral.services {
                for service in services {
                    if let characteristic = getCharacteristic(service.uuid.uuidString, characteristicId: uuidString) {
                        mDiscoverDescriptorsPromise = RNPromise(resolver, rejecter)
                        peripheral.discoverDescriptors(for: characteristic)
                        break
                    }
                }
            }
        }
    }
    
    @objc(writeCharacteristic:uuidString:base64Encoded:resolver:rejecter:)
    func writeCharacteristic(_ serviceId: String, uuidString: String, base64Encoded: String, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        if let peripheral = _currentPeripheral {
            if let characteristic = getCharacteristic(serviceId, characteristicId: uuidString) {
                if let data = Data(base64Encoded: base64Encoded) {
                    mWriteCharacteristicPromise = RNPromise(resolver, rejecter)
                    peripheral.writeValue(data, for: characteristic, type: .withoutResponse)
                }
            }
        }
    }

    @objc(writeDescriptor:characteristicId:uuidString:base64Encoded:resolver:rejecter:)
    func writeDescriptor(_ serviceId: String, characteristicId: String, uuidString: String, base64Encoded: String, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        if let peripheral = _currentPeripheral {
            if let descriptor = getDescriptor(serviceId, characteristicId: characteristicId, uuidString: uuidString) {
                if let data = Data(base64Encoded: base64Encoded) {
                    mWriteDescriptorPromise = RNPromise(resolver, rejecter)
                    peripheral.writeValue(data, for: descriptor)
                }
            }
        }
    }

    @objc(readCharacteristic:uuidString:resolver:rejecter:)
    func readCharacteristic(_ serviceId: String, uuidString: String, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        if let peripheral = _currentPeripheral {
            if let characteristic = getCharacteristic(serviceId, characteristicId: uuidString) {
                mReadCharacteristicPromise = RNPromise(resolver, rejecter)
                peripheral.readValue(for: characteristic)
            }
        }
    }
    
    @objc(readDescriptor:characteristicId:uuidString:resolver:rejecter:)
    func readDescriptor(_ serviceId: String, characteristicId: String, uuidString: String, resolver: @escaping RCTPromiseResolveBlock, rejecter: @escaping RCTPromiseRejectBlock) {
        if let peripheral = _currentPeripheral {
            if let descriptor = getDescriptor(serviceId, characteristicId: characteristicId, uuidString: uuidString) {
                mReadDescriptorPromise = RNPromise(resolver, rejecter)
                peripheral.readValue(for: descriptor)
            }
        }
    }
    
    //MARK: Private method
    private func getCharacteristic(_ serviceId: String, characteristicId: String) -> CBCharacteristic? {
        if let peripheral = _currentPeripheral {
            if let services = peripheral.services {
                for service in services {
                    if service.uuid.uuidString == serviceId {
                        if let characteristics = service.characteristics {
                            for characteristic in characteristics {
                                if characteristic.uuid.uuidString == characteristicId {
                                    return characteristic
                                }
                            }
                        }
                    }
                }
            }
        }
        return nil
    }
    
    private func getDescriptor(_ serviceId: String, characteristicId: String, uuidString: String) -> CBDescriptor? {
        if let characteristic = getCharacteristic(serviceId, characteristicId: characteristicId) {
            if let descriptors = characteristic.descriptors {
                for descriptor in descriptors {
                    if descriptor.uuid.uuidString == uuidString {
                        return descriptor
                    }
                }
            }
        }
        return nil
    }

    //MARK: CBCentralManagerDelegate event
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOn:
            sendEvent(withName: "CBCentralManagerPowerOn", body: nil)
            break
        case .poweredOff:
            sendEvent(withName: "CBCentralManagerPowerOff", body: nil)
            break
        case .unauthorized:
            sendEvent(withName: "CBCentralManagerUnauthorized", body: nil)
            break
        default:
            break
        }
    }
    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        peripheral.delegate = self
        _currentPeripheral = peripheral
        sendEvent(withName: "Connected", body: nil)
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        _currentPeripheral = nil
        sendEvent(withName: "Disconnected", body: nil)
    }
    
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        peripheral.delegate = nil
        _currentPeripheral = nil
        sendEvent(withName: "Disconnected", body: nil)
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        let identifier = peripheral.identifier.uuidString
        let localName = advertisementData[CBAdvertisementDataLocalNameKey] as? String ?? "Unknown Device"
        let txPower = advertisementData[CBAdvertisementDataTxPowerLevelKey] as? NSNumber ?? NSNumber.init(value: 0)
        let manufacturerData = advertisementData[CBAdvertisementDataManufacturerDataKey] as? Data
        var payloads: [AnyHashable: Any?] = [
            "identifier": identifier,
            "name": localName,
            "TxPowerLevel": txPower.intValue,
            "RSSI": RSSI.intValue,
            "ManufacturerSpecificData": nil
        ]
        
        if let data = manufacturerData {
            let manufacturerId = UInt16(data[0]) + UInt16(data[1]) << 8
            let specificData = data[2...]
            if let companyIds = self._companyIds {
                for companyId in companyIds {
                    if companyId.uint16Value == manufacturerId {
                        if data.count > 0 {
                            payloads.updateValue(specificData.base64EncodedString(), forKey: "ManufacturerSpecificData")
                        } else {
                            payloads.updateValue(nil, forKey: "ManufacturerSpecificData")
                        }
                    }
                }
            } else {
                payloads.updateValue(manufacturerId, forKey: "ManufacturerId")
                if data.count > 0 {
                    payloads.updateValue(specificData.base64EncodedString(), forKey: "ManufacturerSpecificData")
                } else {
                    payloads.updateValue(nil, forKey: "ManufacturerSpecificData")
                }
            }
        }
        _deviceMap.updateValue(peripheral, forKey: identifier)
        sendEvent(withName: "FoundBLEDevice", body: payloads)
    }
    
    //MARK: CBPeripheralManagerDelegate event
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        switch peripheral.state {
        case .poweredOn:
            sendEvent(withName: "CBPeripheralManagerPowerOn", body: nil)
            break
        case .poweredOff:
            sendEvent(withName: "CBPeripheralManagerPowerOff", body: nil)
            break
        case .unauthorized:
            sendEvent(withName: "CBPeripheralManagerUnauthorized", body: nil)
            break
        default:
            break
        }
    }
    
    func peripheralManagerDidStartAdvertising(_ peripheral: CBPeripheralManager, error: Error?) {
    }

    //MARK: CBPeripheralDelegate event
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if let error = error {
            if let promise = mDiscoverServicesPromise {
                promise.rejecter("E_GATT_ERROR", "Cannot be found service(s).", error)
            }
        } else {
            var payload: [String] = []
            if let services = peripheral.services {
                for service in services {
                    payload.append(service.uuid.uuidString)
                }
            }
            if let promise = mDiscoverServicesPromise {
                promise.resolver(payload)
            }
        }
        mDiscoverServicesPromise = nil
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        if let error = error {
            if let promise = mDiscoverCharacteristicsPromise {
                promise.rejecter("E_GATT_ERROR", "Cannot be found characteristic(s).", error)
            }
        } else {
            var payload: [String] = []
            if let characteristics = service.characteristics {
                for characteristic in characteristics {
                    payload.append(characteristic.uuid.uuidString)
                }
            }
            
            if let promise = mDiscoverCharacteristicsPromise {
                promise.resolver(payload)
            }
        }
        mDiscoverCharacteristicsPromise = nil
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverDescriptorsFor characteristic: CBCharacteristic, error: Error?) {
        if let error = error {
            if let promise = mDiscoverDescriptorsPromise {
                promise.rejecter("E_GATT_ERROR", "Cannot be found descriptor(s).", error)
            }
        } else {
            var payload: [String] = []
            if let descriptors = characteristic.descriptors {
                for descriptor in descriptors {
                    payload.append(descriptor.uuid.uuidString)
                }
            }
            
            if let promise = mDiscoverDescriptorsPromise {
                promise.resolver(payload)
            }
        }
        mDiscoverDescriptorsPromise = nil
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        if let error = error {
            if let promise = mReadCharacteristicPromise {
                promise.rejecter("E_GATT_ERROR", "Cannot be read characteristic.", error)
            }
        } else {
            var payload: [AnyHashable: Any?] = [:]
            payload.updateValue(characteristic.uuid.uuidString, forKey: "uuid")
            if let data = characteristic.value {
                payload.updateValue(data.base64EncodedString(), forKey: "data")
            } else {
                payload.updateValue(nil, forKey: "data")
            }
            payload.updateValue(nil, forKey: "descriptors")
            if let promise = mReadCharacteristicPromise {
                promise.resolver(payload)
            }
        }
        mReadCharacteristicPromise = nil
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor descriptor: CBDescriptor, error: Error?) {
        if let error = error {
            if let promise = mReadDescriptorPromise {
                promise.rejecter("E_GATT_ERROR", "Cannot be read descriptor.", error)
            }
        } else {
            var payload: [AnyHashable: Any?] = [:]
            payload.updateValue(descriptor.uuid.uuidString, forKey: "uuid")
            if let data = descriptor.value as? Data {
                payload.updateValue(data.base64EncodedString(), forKey: "data")
            } else {
                payload.updateValue(nil, forKey: "data")
            }
            if let promise = mReadDescriptorPromise {
                promise.resolver(payload)
            }
        }
        mReadDescriptorPromise = nil
    }
}
