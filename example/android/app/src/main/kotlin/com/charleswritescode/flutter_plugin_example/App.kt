package com.charleswritescode.flutter_plugin_example

import com.charleswritescode.flutter_plugin.BackgroundWorkService
import com.charleswritescode.flutter_plugin.FlutterPlugin
import io.flutter.app.FlutterApplication
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugins.GeneratedPluginRegistrant

class App: FlutterApplication(), PluginRegistry.PluginRegistrantCallback {
    override fun onCreate() {
        super.onCreate()
        BackgroundWorkService.setPluginRegistrant(this)
    }
    override fun registerWith(p0: PluginRegistry?) {
        GeneratedPluginRegistrant.registerWith(p0)
    }

}