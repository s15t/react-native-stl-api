package kr.co.smartsignal.api.register;

import com.facebook.react.bridge.ReactApplicationContext;
import kr.co.smartsignal.api.NativeRegisterApiSpec;

abstract class RegisterApiSpec extends NativeRegisterApiSpec {
  RegisterApiSpec(ReactApplicationContext context) {
    super(context);
  }
}
