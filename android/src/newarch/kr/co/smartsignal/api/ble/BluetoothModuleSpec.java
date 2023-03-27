package kr.co.smartsignal.api.ble;

import com.facebook.react.bridge.ReactApplicationContext;
import kr.co.smartsignal.api.NativeBluetoothModuleSpec;

import java.util.Map;

abstract class BluetoothModuleSpec extends NativeBluetoothModuleSpec {
  BluetoothModuleSpec(ReactApplicationContext context) {
    super(context);
  }
}
