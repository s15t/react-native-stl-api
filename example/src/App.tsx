import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  PermissionsAndroid,
  TouchableOpacity,
} from 'react-native';
import STL from 'react-native-stl-api';
import { useState } from 'react';

const { ACCESS_FINE_LOCATION } = PermissionsAndroid.PERMISSIONS;

export default function App() {
  const [isFound, setIsFound] = useState<boolean>(false);
  const [bleId, setBLEId] = useState<string | undefined>();
  const [bleType, setBLEType] = useState<number | undefined>();
  const [result, setResult] = React.useState<string[]>([]);

  const touchHandler = async () => {
    try {
      const { id, type } = await STL.register.getNearbyDevice();
      console.log(id, type);
      setIsFound(true);
      setBLEType(type);
      setBLEId(id);
    } catch (err) {
      console.log(err);
      setIsFound(false);
    }
  };

  React.useEffect(() => {
    PermissionsAndroid.requestMultiple([ACCESS_FINE_LOCATION]).then(() => {});
    STL.common.getKeyHashes().then((keyHashes) => setResult(keyHashes));
  }, []);

  return (
    <View style={styles.container}>
      <Text>앱 이름: {STL.common.name}</Text>
      <Text>
        앱 버전 (빌드 버전):{' '}
        {`${STL.common.version} (${STL.common.buildVersion})`}
      </Text>
      <Text>번들 ID: {STL.common.identifier}</Text>
      <View style={{ marginBottom: 12 }} />
      <Text>키 해시 리스트</Text>
      {result.map((keyHash, idx) => (
        <View key={keyHash}>
          <Text>{`KeyHash-${idx + 1}: ${keyHash}`}</Text>
        </View>
      ))}
      <View style={{ marginBottom: 12 }} />
      <View>
        <TouchableOpacity onPress={touchHandler}>
          <Text style={{fontSize: 24, lineHeight: 36, color: '#bd93f9'}}>근처 장비 찾기</Text>
        </TouchableOpacity>
        {isFound && (
          <View>
            <Text>type: {bleType}</Text>
            <Text>id: {bleId}</Text>
          </View>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
