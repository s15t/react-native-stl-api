package kr.co.smartsignal.api.ble;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;

import javax.annotation.Nullable;
import java.util.Map;

abstract class BluetoothModuleSpec extends ReactContextBaseJavaModule {
  BluetoothModuleSpec(ReactApplicationContext context) {
    super(context);
  }

  protected abstract Map<String, Object> getTypedExportedConstants();

  public final @Nullable Map<String, Object> getConstants() {
    return getTypedExportedConstants();
  }
}
