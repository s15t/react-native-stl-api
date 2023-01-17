/**
 * BluetoothScreen
 */

import React, { useEffect, useState } from 'react';
import { Button, StyleSheet, Text, View } from 'react-native';
import STL from 'react-native-stl-api';

export default function BluetoothScreen() {
  const [isBluetooth, setIsBluetooth] = useState(false);

  const checkPermission = async () => {
    const result = await STL.ble.checkScanPermission();
    setIsBluetooth(result);
  };

  const requestPermission = async () => {
    await STL.ble.requestScanPermissions();
  };

  const isDiscovering = async () => {
    const result = await STL.ble.isDiscovering();
    console.log(result);
  };

  const scanStart = async () => {
    if (isBluetooth) {
      try {
        await STL.ble.startScan();
      } catch (e) {
        console.log(e);
      }
    }
  };

  const stopScan = async () => {
    if (isBluetooth) {
      await STL.ble.stopScan();
    }
  };

  useEffect(() => {
    STL.ble.emitter.addListener(STL.ble.eventType.ON_FOUND, (data) => { console.log(data); });
    checkPermission().then(() => false);

    return () => {
      STL.ble.emitter.removeAllListeners(STL.ble.eventType.ON_FOUND);
    };
  }, []);

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>블루투스</Text>
      </View>
      <View style={styles.controlBox}>
        <Button title={'권한 확인'} onPress={checkPermission} />
        <Button title={'권한 요청'} onPress={requestPermission} />
        <Button title={'스캔 여부'} onPress={isDiscovering} />
        <Button title={'스캔 시작'} onPress={scanStart} />
        <Button title={'스캔 종료'} onPress={stopScan} />
      </View>
      <View style={styles.consoleBox}>
        <Text style={styles.console}>
          블루투스 권한: {isBluetooth ? '있음' : '없음'}
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'flex-start',
    alignItems: 'stretch',
    paddingHorizontal: 12,
    paddingTop: 12,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
  },
  controlBox: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  consoleBox: {
    marginVertical: 12,
    justifyContent: 'center',
    alignItems: 'center',
  },
  console: {
    fontSize: 18,
  },
});
