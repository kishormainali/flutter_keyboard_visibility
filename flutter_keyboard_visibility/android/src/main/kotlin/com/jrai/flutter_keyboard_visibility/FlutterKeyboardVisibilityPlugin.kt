package com.jrai.flutter_keyboard_visibility

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel

class FlutterKeyboardVisibilityPlugin : FlutterPlugin, ActivityAware, EventChannel.StreamHandler, ViewTreeObserver.OnGlobalLayoutListener {
    private var eventSink: EventChannel.EventSink? = null
    private var mainView: View? = null
    private var isVisible: Boolean = false

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        init(flutterPluginBinding.binaryMessenger)
    }

    private fun init(messenger: BinaryMessenger) {
        val eventChannel = EventChannel(messenger, "flutter_keyboard_visibility")
        eventChannel.setStreamHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        unregisterListener()
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        listenForKeyboard(binding.activity)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        unregisterListener()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        listenForKeyboard(binding.activity)
    }

    override fun onDetachedFromActivity() {
        unregisterListener()
    }

    override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink?) {
        this.eventSink = eventSink
    }

    override fun onCancel(arguments: Any?) {
        this.eventSink = null
    }

    override fun onGlobalLayout() {
        mainView?.let { view ->
            val r = Rect()
            view.getWindowVisibleDisplayFrame(r)

            // check if the visible part of the screen is less than 85%
            // if it is then the keyboard is showing
            val newState = r.height().toDouble() / view.rootView.height.toDouble() < 0.85

            if (newState != isVisible) {
                isVisible = newState
                eventSink?.success(if (isVisible) 1 else 0)
            }
        }
    }

    private fun listenForKeyboard(activity: Activity) {
        mainView = activity.findViewById(android.R.id.content)
        mainView?.viewTreeObserver?.addOnGlobalLayoutListener(this)
    }

    private fun unregisterListener() {
        mainView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
        mainView = null
    }
}
