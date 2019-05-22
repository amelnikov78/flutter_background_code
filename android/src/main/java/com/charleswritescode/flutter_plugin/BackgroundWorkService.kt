package com.charleswritescode.flutter_plugin

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.preference.PreferenceManager
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain
import io.flutter.view.FlutterNativeView
import io.flutter.view.FlutterRunArguments

class BackgroundWorkService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private lateinit var backgroundMethodChannel: MethodChannel

    private var flutterView: FlutterNativeView? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if(intent!!.action == "EXECUTE_CODE") {
            val codeCallbackHandle = intent.getLongExtra(FlutterPlugin.CODE_CALLBACK_HANDLE_KEY,0)
            val num1 = intent.getLongExtra("num1",-1)
            val num2 = intent.getLongExtra("num2", -1)

            backgroundMethodChannel.invokeMethod("", listOf(codeCallbackHandle, num1, num2), object : MethodChannel.Result {
                override fun notImplemented() {
                    println("method not implemented")
                }

                override fun error(p0: String?, p1: String?, p2: Any?) {
                    println("ERROR: ${p0} ${p1} ${p2}")
                }

                override fun success(result: Any?) {
                    println("Result of operation: $result")
                }

            })
        }

        return START_STICKY_COMPATIBILITY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate() {
        super.onCreate()
        initFlutterService()
    }


    private fun initFlutterService() {


        val callbackHandle = PreferenceManager.getDefaultSharedPreferences(this).getLong(FlutterPlugin.DISPATCHER_CALLBACK_HANDLE_KEY, -1)
        val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)

        if (flutterView == null) {

            flutterView = FlutterNativeView(this, true)


            val args = FlutterRunArguments()
            args.bundlePath = FlutterMain.findAppBundlePath(this)
            args.entrypoint = callbackInfo.callbackName
            args.libraryPath = callbackInfo.callbackLibraryPath

            println("Starting flutter background view")
            flutterView!!.runFromBundle(args)

        }

        backgroundMethodChannel = MethodChannel(flutterView, "flutter_plugin_background")
        backgroundMethodChannel.setMethodCallHandler { methodCall, result ->
            when (methodCall.method) {
                "initialized" -> {
                    result.success("")
                }
                else -> {
                    print("Not implemented ${methodCall.method}")
                    result.notImplemented()
                }
            }
        }
    }
}
