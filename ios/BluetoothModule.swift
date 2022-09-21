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
    private var _companyId: NSNumber?
    private lazy var _manager = CBCentralManager(delegate: self, queue: DispatchQueue.main)
    private lazy var _peripheralManager = CBPeripheralManager(delegate: self, queue: DispatchQueue.main)
    
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
            "FoundBLEDevice"
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
    func startScanByCompanyId(_ companyId: NSNumber) {
        if !_manager.isScanning {
            _companyId = companyId
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
    
    @objc(writeCharacteristic:base64Encoded:)
    func writeCharacteristic(_ uuidString: String, base64Encoded: String) {
        if let peripheral = _currentPeripheral {
            let characteristic = CBMutableCharacteristic(type: CBUUID(string: uuidString), properties: .notify, value: nil, permissions: .writeable)
            peripheral.writeValue(Data(base64Encoded: base64Encoded)!, for: characteristic, type: .withResponse)
        }
    }

    @objc(writeDescriptor:base64Encoded:)
    func writeDescriptor(_ uuidString: String, base64Encoded: String) {
        if let peripheral = _currentPeripheral {
            peripheral.writeValue(Data(base64Encoded: base64Encoded)!, for: CBMutableDescriptor(type: CBUUID(string: uuidString), value: nil))
        }
    }

    @objc(readCharacteristic:)
    func readCharacteristic(_ uuidString: String) {
        if let peripheral = _currentPeripheral {
            let characteristic = CBMutableCharacteristic(type: CBUUID(string: uuidString), properties: .notify, value: nil, permissions: .writeable)
            peripheral.readValue(for: characteristic)
        }
    }
    
    @objc(readDescriptor:)
    func readDescriptor(_ uuidString: String) {
        if let peripheral = _currentPeripheral {
            peripheral.readValue(for: CBMutableDescriptor(type: CBUUID(string: uuidString), value: nil))
        }
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
        _currentPeripheral = peripheral
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        _currentPeripheral = nil
    }
    
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        _currentPeripheral = nil
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
            payloads.updateValue(manufacturerId, forKey: "ManufacturerId")
            if data.count > 2 {
                payloads.updateValue(data[2...].base64EncodedString(), forKey: "ManufacturerSpecificData")
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
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverDescriptorsFor characteristic: CBCharacteristic, error: Error?) {
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        var descriptorMap: [AnyHashable: Any?] = [:]
        var payloads: [AnyHashable: Any?] = [
            "uuid": characteristic.uuid.uuidString
        ]
        if let descriptors = characteristic.descriptors {
            for descriptor in descriptors {
                if let data = descriptor.value as? Data {
                    descriptorMap.updateValue(data.base64EncodedString(), forKey: descriptor.uuid.uuidString)
                } else {
                    descriptorMap.updateValue(nil, forKey: descriptor.uuid.uuidString)
                }
            }
        }
        sendEvent(withName: "CharacteristicRead", body: payloads)
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor descriptor: CBDescriptor, error: Error?) {
        var payloads: [AnyHashable: Any?] = [
            "uuid": descriptor.uuid.uuidString,
            "data": nil
        ]
        if let data = descriptor.value as? Data {
            payloads.updateValue(data.base64EncodedString(), forKey: "data")
        }
        sendEvent(withName: "DescriptorRead", body: payloads)
    }
}
