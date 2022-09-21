import * as React from 'react';

import { StyleSheet, View, Text, Button, Image } from 'react-native';
import STL from 'react-native-stl-api';
import { useState } from 'react';

export default function App() {
  const [colorMode, setColorMode] = useState<string>('');
  const [keyHashes, setKeyHashes] = useState<string[]>([]);

  const updateColorMode = async () => {
    try {
      const mode = await STL.common.getColorMode();
      switch (mode) {
        case STL.COLOR_MODE.DEFAULT:
          setColorMode('sRGB');
          break;
        case STL.COLOR_MODE.WIDE_COLOR_GAMUT:
          setColorMode('Display P3');
          break;
        case STL.COLOR_MODE.HDR:
          setColorMode('HDR');
          break;
      }
    } catch (err) {}
  };

  const changeColorMode = async (mode: number) => {
    try {
      await STL.common.setColorMode(mode);
      await updateColorMode();
    } catch (err) {}
  };

  React.useEffect(() => {
    changeColorMode(STL.COLOR_MODE.WIDE_COLOR_GAMUT).then(() => false);
    updateColorMode().then(() => false);
    STL.common.getKeyHashes().then((hashes) => setKeyHashes(hashes));
  }, []);

  return (
    <View style={styles.container}>
      <Text>앱 이름: {STL.common.name}</Text>
      <Text>
        앱 버전 (빌드 버전):{' '}
        {`${STL.common.version} (${STL.common.buildVersion})`}
      </Text>
      <Text>번들 ID: {STL.common.identifier}</Text>
      <View style={styles.horizontal} />
      <Text>키 해시 리스트</Text>
      {keyHashes.map((keyHash, idx) => (
        <View key={keyHash}>
          <Text>{`KeyHash-${idx + 1}: ${keyHash}`}</Text>
        </View>
      ))}
      <View style={styles.horizontal} />
      <Text>현재 색상: {colorMode}</Text>
      <Text>색상 모드 설정</Text>
      <View style={[styles.row]}>
        <View style={styles.colorGamutButton}>
          <Button
            title={'sRGB'}
            onPress={() => changeColorMode(STL.COLOR_MODE.DEFAULT)}
          />
        </View>
        <View style={styles.colorGamutButton}>
          <Button
            title={'Display P3'}
            onPress={() => changeColorMode(STL.COLOR_MODE.WIDE_COLOR_GAMUT)}
          />
        </View>
        <View style={styles.colorGamutButton}>
          <Button
            title={'HDR'}
            onPress={() => changeColorMode(STL.COLOR_MODE.HDR)}
          />
        </View>
      </View>
      <Image
        source={require('./assets/Webkit-logo-P3.png')}
        style={styles.P3Image}
      />
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
  P3Image: {
    marginTop: 12,
    width: 250,
    height: 250,
  },
  horizontal: {
    marginBottom: 12,
  },
  colorGamutButton: {
    paddingHorizontal: 6,
  },
  row: {
    flexDirection: 'row',
  },
  column: {
    flexDirection: 'column',
  },
});
