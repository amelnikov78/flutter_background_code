import 'dart:async';
import 'dart:ui';

import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

class FlutterPlugin {
  static const MethodChannel _channel = const MethodChannel('flutter_plugin');

  static const MethodChannel _backroundChannel =
  MethodChannel("flutter_plugin_background");

  static Future<void> initBackgroundService() async {
    CallbackHandle handle = PluginUtilities.getCallbackHandle(callbackDispatcher);
    await _channel.invokeMethod("initService", [handle.toRawHandle()]);
  }

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static void executeCode(int num1, int num2) {
    CallbackHandle codeCallbackHandle = PluginUtilities.getCallbackHandle(addNumbers);
    _channel.invokeMethod("executeCode", [codeCallbackHandle.toRawHandle(), num1, num2]);
  }
}

int addNumbers(int num1, int num2) {
  return num1+num2;
}

void callbackDispatcher() {

    WidgetsFlutterBinding.ensureInitialized();

  FlutterPlugin._backroundChannel.setMethodCallHandler((call) async {
    Function callback = PluginUtilities.getCallbackFromHandle(CallbackHandle.fromRawHandle(call.arguments[0] as int));

    int num1 = call.arguments[1];
    int num2 = call.arguments[2];

    return callback(num1, num2);
  });
  FlutterPlugin._backroundChannel.invokeMethod("initialized");
}
