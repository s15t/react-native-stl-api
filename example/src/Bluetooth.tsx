/**
 * BluetoothScreen
 */

import React, { useEffect, useRef, useState } from 'react';
import {
  Alert,
  Button,
  Platform,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { Base64 } from 'js-base64';
// @ts-expect-error
import STL from '@stl1/react-native-stl-api';

export default function BluetoothScreen() {
  const [isBluetooth, setIsBluetooth] = useState(false);
  const [isScan, setIsScan] = useState(false);
  const [logs, setLogs] = useState<string[]>([]);
  const scrollRef = useRef<ScrollView>(null);

  const checkPermission = () => {
    (async () => {
      if (Platform.OS === 'android') {
        try {
          const result = await STL.ble.checkScanPermission();
          setIsBluetooth(result);
        } catch (err) {
          console.error(err);
        }
      } else if (Platform.OS === 'ios') {
        setIsBluetooth(STL.ble.checkPermission());
      }
    })();
  };

  const requestPermission = () => {
    (async () => {
      if (Platform.OS === 'android') {
        try {
          await STL.ble.requestScanPermissions();
        } catch (err) {
          console.error(err);
        }
      } else if (Platform.OS === 'ios') {
        Alert.alert(
          '블루투스 권한 요청',
          "설정에서 블루투스 권한을 허용해주세요.\n\n설정 > 개인정보 보호 및 보안 > Bluetooth\n\n'확인'을 누르면 설정화면으로 이동합니다.",
          [
            {
              text: '확인',
              onPress: () => {
                STL.common.navigateToSettings();
              },
            },
          ]
        );
      }
    })();
  };

  const isDiscovering = () => {
    (async () => {
      if (Platform.OS === 'android') {
        const result = await STL.ble.isDiscovering();
        setIsScan(result);
      } else if (Platform.OS === 'ios') {
        const result = STL.ble.isScanning();
        setIsScan(result);
      }
    })();
  };

  const scanStart = async () => {
    if (isBluetooth) {
      if (Platform.OS === 'android') {
        try {
          await STL.ble.startScan();
        } catch (err) {}
      } else if (Platform.OS === 'ios') {
        STL.ble.startScan();
      }
      setTimeout(() => isDiscovering(), 100);
    } else {
      requestPermission();
    }
  };

  const stopScan = async () => {
    if (isBluetooth) {
      if (Platform.OS === 'android') {
        await STL.ble.stopScan();
      } else if (Platform.OS === 'ios') {
        STL.ble.stopScan();
      }
      setTimeout(() => isDiscovering(), 100);
    } else {
      requestPermission();
    }
  };

  const appendLogText = (log: string) => {
    setLogs((prevLog) => [...prevLog, log]);
  };

  useEffect(() => {
    STL.ble.emitter.addListener('FoundBLEDevice', (data: any) => {
      if (data.ManufacturerSpecificData) {
        const buffer = Base64.toUint8Array(data.ManufacturerSpecificData);
        // buffer to hexstring
        data.ManufacturerSpecificData = Array.from(buffer)
          .map((byte) => byte.toString(16).padStart(2, '0'))
          .join(' ');
      }
      appendLogText(JSON.stringify(data, Object.keys(data).sort(), 2));
    });
    checkPermission();

    return () => {
      STL.ble.emitter.removeAllListeners('FoundBLEDevice');
    };
  }, []);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollToEnd({ animated: true });
    }
  }, [logs]);

  return (
    <SafeAreaView style={styles.safeAreaView}>
      <View style={styles.container}>
        <View style={styles.header}>
          <Text style={styles.title}>블루투스</Text>
        </View>
        <View style={styles.controlBox}>
          {!isBluetooth && (
            <Button
              title={'블루투스 권한 요청'}
              onPress={() => requestPermission()}
            />
          )}
          {isScan ? (
            <Button title={'스캔 종료'} onPress={() => stopScan()} />
          ) : (
            <Button title={'스캔 시작'} onPress={() => scanStart()} />
          )}
        </View>
        <View style={styles.consoleBox}>
          <Text style={styles.console}>
            블루투스 권한: {isBluetooth ? '있음' : '없음'}
          </Text>
          <Text style={styles.console}>
            블루투스 스캔 상태: {isScan ? '스캔 중' : '스캔 중지'}
          </Text>
        </View>
        <View style={styles.logControl}>
          <Button
            title={'로그 초기화'}
            disabled={logs.length === 0}
            onPress={() => setLogs([])}
          />
        </View>
        <ScrollView ref={scrollRef} style={styles.logView}>
          <View>
            {logs.map((log, index) => (
              <Text key={index} style={styles.logText}>
                {log}
              </Text>
            ))}
          </View>
        </ScrollView>
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeAreaView: {
    flex: 1,
  },
  container: {
    flex: 1,
    justifyContent: 'flex-start',
    alignItems: 'stretch',
    marginHorizontal: 12,
    paddingTop: 10,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  title: {
    fontSize: 42,
    fontWeight: 'bold',
  },
  controlBox: {
    flexDirection: 'row',
    justifyContent: 'center',
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
  logView: {
    flex: 1,
    padding: 10,
    marginBottom: 14,
    backgroundColor: '#fff',
    borderRadius: 15,
  },
  logControl: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
  },
  logText: {
    fontSize: 15,
  },
});
