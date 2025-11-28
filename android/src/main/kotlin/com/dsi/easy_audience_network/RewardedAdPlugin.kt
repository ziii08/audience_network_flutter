package com.dsi.easy_audience_network

import android.content.Context
import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.RewardData
import com.facebook.ads.RewardedVideoAd
import com.facebook.ads.RewardedVideoAdListener
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.util.HashMap

class RewardedAdPlugin(
    private val context: Context,
    private val channel: MethodChannel
) : MethodChannel.MethodCallHandler {

    private val adsById = HashMap<Int, RewardedVideoAd>()
    private val delayHandler = Handler(Looper.getMainLooper())
    private var activity: Activity? = null

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            Constants.LOAD_REWARDED_VIDEO_METHOD -> {
                result.success(loadAd(call.arguments as HashMap<String, Any>))
            }
            Constants.SHOW_REWARDED_VIDEO_METHOD -> {
                result.success(showAd(call.arguments as HashMap<String, Any>))
            }
            Constants.DESTROY_REWARDED_VIDEO_METHOD -> {
                result.success(destroyAd(call.arguments as HashMap<String, Any>))
            }
            else -> result.notImplemented()
        }
    }

    private fun loadAd(args: HashMap<String, Any>): Boolean {
        val id = args["id"] as Int
        val placementId = args["placementId"] as String
        val userId = args["userId"] as? String

        var rewardedVideoAd = adsById[id]
        if (rewardedVideoAd == null) {
            Log.d("RewardedAdPlugin", "Creating RewardedVideoAd with activity: $activity")
            rewardedVideoAd = RewardedVideoAd(activity ?: context, placementId)
            adsById[id] = rewardedVideoAd
        }

        try {
            val rewardData = RewardData(userId ?: "", null)
            if (!rewardedVideoAd.isAdLoaded) {
                // Create a dedicated listener for this ad instance to capture the ID
                val listener = createAdListener(id)
                
                val loadAdConfig = rewardedVideoAd.buildLoadAdConfig()
                    .withAdListener(listener)
                    .withRewardData(rewardData)
                    .build()
                rewardedVideoAd.loadAd(loadAdConfig)
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun createAdListener(id: Int): RewardedVideoAdListener {
        return object : RewardedVideoAdListener {
            override fun onRewardedVideoCompleted() {
                val args = hashMapOf<String, Any?>(
                    "id" to id,
                    // Cannot access ad object here directly, but we know the ID
                    "placement_id" to adsById[id]?.placementId,
                    "invalidated" to adsById[id]?.isAdInvalidated
                )
                channel.invokeMethod(Constants.REWARDED_VIDEO_COMPLETE_METHOD, args)
            }

            override fun onRewardedVideoClosed() {
                val args = hashMapOf<String, Any?>(
                    "id" to id,
                    "placement_id" to adsById[id]?.placementId,
                    "invalidated" to adsById[id]?.isAdInvalidated
                )
                channel.invokeMethod(Constants.REWARDED_VIDEO_CLOSED_METHOD, args)
            }

            override fun onError(ad: Ad?, adError: AdError?) {
                val args = hashMapOf<String, Any?>(
                    "id" to id,
                    "placement_id" to ad?.placementId,
                    "invalidated" to ad?.isAdInvalidated,
                    "error_code" to adError?.errorCode,
                    "error_message" to adError?.errorMessage
                )
                channel.invokeMethod(Constants.ERROR_METHOD, args)
            }

            override fun onAdLoaded(ad: Ad?) {
                val args = hashMapOf<String, Any?>(
                    "id" to id,
                    "placement_id" to ad?.placementId,
                    "invalidated" to ad?.isAdInvalidated
                )
                channel.invokeMethod(Constants.LOADED_METHOD, args)
            }

            override fun onAdClicked(ad: Ad?) {
                val args = hashMapOf<String, Any?>(
                    "id" to id,
                    "placement_id" to ad?.placementId,
                    "invalidated" to ad?.isAdInvalidated
                )
                channel.invokeMethod(Constants.CLICKED_METHOD, args)
            }

            override fun onLoggingImpression(ad: Ad?) {
                val args = hashMapOf<String, Any?>(
                    "id" to id,
                    "placement_id" to ad?.placementId,
                    "invalidated" to ad?.isAdInvalidated
                )
                channel.invokeMethod(Constants.LOGGING_IMPRESSION_METHOD, args)
            }
        }
    }

    private fun showAd(args: HashMap<String, Any>): Boolean {
        val id = args["id"] as Int
        val delay = args["delay"] as? Int ?: 0
        val rewardedVideoAd = adsById[id]

        if (rewardedVideoAd == null || !rewardedVideoAd.isAdLoaded) {
            return false
        }

        if (rewardedVideoAd.isAdInvalidated) {
            return false
        }

        if (delay <= 0) {
            rewardedVideoAd.show(rewardedVideoAd.buildShowAdConfig().build())
        } else {
            delayHandler.postDelayed({
                if (rewardedVideoAd.isAdLoaded && !rewardedVideoAd.isAdInvalidated) {
                    rewardedVideoAd.show(rewardedVideoAd.buildShowAdConfig().build())
                }
            }, delay.toLong())
        }
        return true
    }

    private fun destroyAd(args: HashMap<String, Any>): Boolean {
        val id = args["id"] as Int
        val rewardedVideoAd = adsById[id] ?: return false

        rewardedVideoAd.destroy()
        adsById.remove(id)
        return true
    }
}
