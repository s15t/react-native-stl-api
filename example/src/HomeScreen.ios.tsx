/**
 * HomeScreenAndroid
 *
 */

import React from 'react';
import { StyleSheet, View, Text } from 'react-native';
// @ts-expect-error
import STL from '@stl1/react-native-stl-api';

export default function HomeScreen() {
  return (
    <View style={styles.container}>
      <Text>앱 이름: {STL.common.name}</Text>
      <Text>
        앱 버전 (빌드 버전):{' '}
        {`${STL.common.version} (${STL.common.buildVersion})`}
      </Text>
      <Text>번들 ID: {STL.common.identifier}</Text>
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

