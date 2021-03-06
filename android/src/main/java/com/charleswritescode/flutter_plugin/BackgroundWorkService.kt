package com.charleswritescode.flutter_plugin

import android.app.job.JobParameters
import android.app.job.JobService
import android.preference.PreferenceManager
import android.widget.Toast
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain
import io.flutter.view.FlutterNativeView
import io.flutter.view.FlutterRunArguments

class BackgroundWorkService : JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        startDartBackgroundIsolate(params!!)
        return false
    }

    companion object {

        @JvmStatic
        private lateinit var sPluginRegistrantCallback: PluginRegistry.PluginRegistrantCallback

        @JvmStatic
        fun setPluginRegistrant(callback: PluginRegistry.PluginRegistrantCallback) {
            sPluginRegistrantCallback = callback
        }
    }

    override fun onCreate() {
        super.onCreate()
        println("Initializing Flutter")
        FlutterMain.ensureInitializationComplete(applicationContext, null)
    }

    private lateinit var backgroundMethodChannel: MethodChannel

    private var flutterView: FlutterNativeView? = null

    private var initialized = false


    private fun startDartBackgroundIsolate(params: JobParameters) {

        println("Getting Callback for Dart code execution")
        val callbackHandle = PreferenceManager.getDefaultSharedPreferences(this).getLong(FlutterPlugin.DISPATCHER_CALLBACK_HANDLE_KEY, -1)
        val callbackInfo = FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)

        if(callbackInfo == null) {
            throw  IllegalArgumentException("Could not find callback for ${callbackHandle}")
        }

        if (flutterView == null) {

            println("Creating flutter background view")
            flutterView = FlutterNativeView(applicationContext, true)


            println("Registering plugins")
            val registry = flutterView!!.pluginRegistry
            sPluginRegistrantCallback.registerWith(registry)

            println("Creating flutter view start arguments")
            val args = FlutterRunArguments()
            args.bundlePath = FlutterMain.findAppBundlePath(applicationContext)
            args.entrypoint = callbackInfo.callbackName
            args.libraryPath = callbackInfo.callbackLibraryPath

            println("Starting flutter background view")
            flutterView!!.runFromBundle(args)

        }

        backgroundMethodChannel = MethodChannel(flutterView, "flutter_plugin_background")
        backgroundMethodChannel.setMethodCallHandler { methodCall, result ->
            when (methodCall.method) {
                "initialized" -> {

                    println("Initialized")
                    initialized = true

                    println("Running dart code in isolate")
                    executeCode(params)

                    result.success("")
                }
                else -> {
                    println("Not implemented ${methodCall.method}")
                    result.notImplemented()
                }
            }
        }
    }

    private fun executeCode(params: JobParameters) {

        val codeCallbackHandle = PreferenceManager.getDefaultSharedPreferences(this).getLong(FlutterPlugin.CODE_CALLBACK_HANDLE_KEY,-1)
        val num1 = params.extras.getLong("num1",-1)
        val num2 = params.extras.getLong("num2", -1)

        backgroundMethodChannel.invokeMethod("", listOf(codeCallbackHandle, num1, num2), object : MethodChannel.Result {
            override fun notImplemented() {
                println("method not implemented")
                jobFinished(params, true)
            }

            override fun error(p0: String?, p1: String?, p2: Any?) {
                println("ERROR: ${p0} ${p1} ${p2}")
                jobFinished(params, true)
            }

            override fun success(result: Any?) {
                println("Result of invokeMethod: $result")
                jobFinished(params, false)
            }

        })

    }
}
