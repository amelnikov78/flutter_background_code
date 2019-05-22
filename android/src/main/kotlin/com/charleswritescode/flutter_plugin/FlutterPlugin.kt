package com.charleswritescode.flutter_plugin

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.PersistableBundle
import android.preference.PreferenceManager
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.util.concurrent.TimeUnit

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
    val num1 = (args[0] as Number).toLong()
    val num2= (args[1] as Number).toLong()

    println("Scheduling job")

    val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    val extras = PersistableBundle()
    extras.putLong("num1", num1)
    extras.putLong("num2", num2)

    jobScheduler.schedule(JobInfo.Builder(0, ComponentName(context, BackgroundWorkService::class.java))
            .setMinimumLatency(TimeUnit.MINUTES.toMillis(1))
            .setExtras(extras)
            .build())
  }

  private fun initBackgroundService(args: List<*>) {

    println("Registering callback handles")

    val callbackHandle = (args[0] as Number).toLong()
    val codeHandle = (args[1] as Number).toLong()


    PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putLong(DISPATCHER_CALLBACK_HANDLE_KEY, callbackHandle)
            .putLong(CODE_CALLBACK_HANDLE_KEY, codeHandle)
            .apply()


  }
}
