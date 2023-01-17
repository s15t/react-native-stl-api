import * as React from 'react';

import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import HomeScreen from './HomeScreen';
import BluetoothScreen from './Bluetooth';

const Tab = createBottomTabNavigator();

export default function App() {
  return (
    <NavigationContainer>
      <Tab.Navigator>
        <Tab.Screen
          name="Home"
          component={HomeScreen}
          options={{
            title: 'Home',
            headerShown: false,
          }}
        />
        <Tab.Screen
          name={'Bluetooth'}
          component={BluetoothScreen}
          options={{
            title: '블루투스',
            headerShown: false,
          }}
        />
      </Tab.Navigator>
    </NavigationContainer>
  );
}
