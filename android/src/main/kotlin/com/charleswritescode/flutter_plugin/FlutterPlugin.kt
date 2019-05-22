package com.charleswritescode.flutter_plugin

import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class FlutterPlugin(val context: Context) : MethodCallHandler {
  companion object {
    @JvmStatic
    val DISPATCHER_CALLBACK_HANDLE_KEY = "DISPATCHER_CALLBACK_HANDLE_KEY"


    @JvmStatic
    val CODE_CALLBACK_HANDLE_KEY = "CODE_CALLBACK_HANDLE_KEY"


    @JvmStatic
    val CODE_EXECUTION_DELAY = "CODE_EXECUTION_DELAY"

    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "flutter_plugin")
      channel.setMethodCallHandler(FlutterPlugin(registrar.context()))
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when(call.method) {
      "initService" -> initBackgroundService(call.arguments as List<*>)
      "executeCode" -> executeCodeInService(call.arguments as List<*>)
      "getPlatformVersion" -> result.success("Android ${android.os.Build.VERSION.RELEASE}")
    }
  }

  private fun executeCodeInService(args: List<*>) {
    println(args)
    context.startService(Intent("EXECUTE_CODE",null,context, BackgroundWorkService::class.java)
            .putExtra(CODE_CALLBACK_HANDLE_KEY, (args[0] as Number).toLong())
            .putExtra("num1", (args[1] as Number).toLong())
            .putExtra("num2", (args[2] as Number).toLong())
    )
  }

  private fun initBackgroundService(args: List<*>) {
    val callbackHandle = args[0] as Long

    PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putLong(DISPATCHER_CALLBACK_HANDLE_KEY, callbackHandle)
            .apply()

    context.startService(Intent("INIT",null,context, BackgroundWorkService::class.java))

  }
}
