package com.dsi.easy_audience_network

import android.app.Activity
import android.content.Context
import androidx.annotation.NonNull
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/**
 * EasyAudienceNetworkPlugin - Main plugin class for Facebook Audience Network
 * 
 * This class handles initialization and manages ad channels for different ad types.
 */
class EasyAudienceNetworkPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private lateinit var channel: MethodChannel
    private lateinit var interstitialAdChannel: MethodChannel
    private lateinit var rewardedAdChannel: MethodChannel
    private var interstitialAdPlugin: InterstitialAdPlugin? = null
    private var rewardedAdPlugin: RewardedAdPlugin? = null
    private var activity: Activity? = null

    private var context: Context? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext

        // Main channel for initialization
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, Constants.MAIN_CHANNEL)
        channel.setMethodCallHandler(this)

        // Interstitial Ad channel
        interstitialAdChannel = MethodChannel(
            flutterPluginBinding.binaryMessenger,
            Constants.INTERSTITIAL_AD_CHANNEL
        )
        interstitialAdPlugin = InterstitialAdPlugin(context!!, interstitialAdChannel)
        interstitialAdChannel.setMethodCallHandler(interstitialAdPlugin)

        // Rewarded Video Ad channel
        rewardedAdChannel = MethodChannel(
            flutterPluginBinding.binaryMessenger,
            Constants.REWARDED_VIDEO_CHANNEL
        )
        rewardedAdPlugin = RewardedAdPlugin(context!!, rewardedAdChannel)
        rewardedAdChannel.setMethodCallHandler(rewardedAdPlugin)

        // Banner Ad platform view
        flutterPluginBinding.platformViewRegistry.registerViewFactory(
            Constants.BANNER_AD_CHANNEL,
            BannerAdFactory(flutterPluginBinding.binaryMessenger)
        )
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            Constants.INIT_METHOD -> init(call.arguments as HashMap<String, Any>, result)
            else -> result.notImplemented()
        }
    }

    private fun init(initValues: HashMap<String, Any>, result: Result) {
        val testingId = initValues["testingId"] as? String
        val testMode = initValues["testMode"] as? Boolean ?: false

        // Add test device if provided
        testingId?.let {
            AdSettings.addTestDevice(it)
        }

        // Enable test mode if requested
        if (testMode) {
            AdSettings.setTestMode(true)
        }

        // Initialize Audience Network SDK
        activity?.let { act ->
            AudienceNetworkAds.buildInitSettings(act.applicationContext)
                .withInitListener { initResult ->
                    result.success(initResult.isSuccess)
                }
                .initialize()
        } ?: run {
            // If activity is not available yet, use context
            context?.let { ctx ->
                AudienceNetworkAds.buildInitSettings(ctx)
                    .withInitListener { initResult ->
                        result.success(initResult.isSuccess)
                    }
                    .initialize()
            } ?: result.error("NO_CONTEXT", "Context not available", null)
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        interstitialAdChannel.setMethodCallHandler(null)
        rewardedAdChannel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        interstitialAdPlugin?.setActivity(activity)
        rewardedAdPlugin?.setActivity(activity)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        // Keep activity reference for config changes
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        interstitialAdPlugin?.setActivity(activity)
        rewardedAdPlugin?.setActivity(activity)
    }

    override fun onDetachedFromActivity() {
        activity = null
        interstitialAdPlugin?.setActivity(null)
        rewardedAdPlugin?.setActivity(null)
    }
}
