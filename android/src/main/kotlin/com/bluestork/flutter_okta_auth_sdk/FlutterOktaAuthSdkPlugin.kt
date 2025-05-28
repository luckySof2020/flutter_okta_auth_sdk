package com.bluestork.flutter_okta_auth_sdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.NonNull
import com.bluestork.flutter_okta_auth_sdk.okta.entities.AvailableMethods
import com.bluestork.flutter_okta_auth_sdk.okta.entities.Errors
import com.bluestork.flutter_okta_auth_sdk.okta.entities.OktaClient
import com.bluestork.flutter_okta_auth_sdk.okta.entities.PendingOperation
import com.bluestork.flutter_okta_auth_sdk.okta.operations.*
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler

/** FlutterOktaAuthSdkPlugin */
class FlutterOktaAuthSdkPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private var applicationContext: Context? = null
    private var mainActivity: Activity? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        this.onAttachedToEngine(flutterPluginBinding.applicationContext, flutterPluginBinding.binaryMessenger)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onDetachedFromActivity() {
        this.mainActivity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        binding.addActivityResultListener { requestCode, resultCode, data ->
            handleActivityResult(requestCode, resultCode, data)
        }
        mainActivity = binding.activity
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        binding.addActivityResultListener { requestCode, resultCode, data ->
            handleActivityResult(requestCode, resultCode, data)
        }
        mainActivity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        this.mainActivity = null
    }

    private fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        try {
            OktaClient.getWebClient().handleActivityResult(requestCode, resultCode, data)
        } catch (_: Exception) {}
        return PendingOperation.hasPendingOperation != null
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        val arguments = call.arguments<Map<String, Any>>()
        PendingOperation.init(call.method, result)

        if (applicationContext == null) {
            PendingOperation.error(Errors.NO_CONTEXT)
            return
        }

        try {
            when (call.method) {
                AvailableMethods.CREATE_CONFIG.methodName -> {
                    if (arguments != null) {
                        createConfig(arguments, applicationContext!!)
                    }
                }
                AvailableMethods.SIGN_IN.methodName -> {
                    signInNew(this.mainActivity!!)
                }
                AvailableMethods.SIGN_OUT.methodName -> {
                    signOut(this.mainActivity!!)
                }
                AvailableMethods.GET_USER.methodName -> {
                    getUser ()
                }
                AvailableMethods.IS_AUTHENTICATED.methodName -> {
                    isAuthenticated()
                }
                AvailableMethods.GET_ACCESS_TOKEN.methodName -> {
                    getAccessToken()
                }
                AvailableMethods.GET_ID_TOKEN.methodName -> {
                    getIdToken()
                }
                AvailableMethods.REVOKE_ACCESS_TOKEN.methodName -> {
                    revokeAccessToken()
                }
                AvailableMethods.REVOKE_ID_TOKEN.methodName -> {
                    revokeIdToken()
                }
                AvailableMethods.REVOKE_REFRESH_TOKEN.methodName -> {
                    revokeRefreshToken()
                }
                AvailableMethods.CLEAR_TOKENS.methodName -> {
                    clearTokens()
                }
                AvailableMethods.INTROSPECT_ACCESS_TOKEN.methodName -> {
                    introspectAccessToken()
                }
                AvailableMethods.INTROSPECT_ID_TOKEN.methodName -> {
                    introspectIdToken()
                }
                AvailableMethods.INTROSPECT_REFRESH_TOKEN.methodName -> {
                    introspectRefreshToken()
                }
                AvailableMethods.REFRESH_TOKENS.methodName -> {
                    refreshTokens()
                }
                else -> {
                    PendingOperation.error(Errors.METHOD_NOT_IMPLEMENTED, "Method called: ${call.method}")
                }
            }
        } catch (ex: Exception) {
            PendingOperation.error(Errors.GENERIC_ERROR, ex.localizedMessage)
        }
    }

    private fun onAttachedToEngine(context: Context, binaryMessenger: BinaryMessenger) {
        applicationContext = context
        channel = MethodChannel(binaryMessenger, "flutter_okta_auth_sdk")
        channel.setMethodCallHandler(this)
    }
}
