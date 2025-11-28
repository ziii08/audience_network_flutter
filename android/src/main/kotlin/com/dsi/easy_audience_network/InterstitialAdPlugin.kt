package com.dsi.easy_audience_network

import android.content.Context
import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.CacheFlag
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.util.HashMap
import java.util.EnumSet

class InterstitialAdPlugin(
    private val context: Context,
    private val channel: MethodChannel
) : MethodChannel.MethodCallHandler, InterstitialAdListener {

    private val adsById = HashMap<Int, InterstitialAd>()
    private val idsByAd = HashMap<InterstitialAd, Int>()
    private val delayHandler = Handler(Looper.getMainLooper())
    private var activity: Activity? = null

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            Constants.LOAD_INTERSTITIAL_METHOD -> {
                result.success(loadAd(call.arguments as HashMap<String, Any>))
            }
            Constants.SHOW_INTERSTITIAL_METHOD -> {
                result.success(showAd(call.arguments as HashMap<String, Any>))
            }
            Constants.DESTROY_INTERSTITIAL_METHOD -> {
                result.success(destroyAd(call.arguments as HashMap<String, Any>))
            }
            else -> result.notImplemented()
        }
    }

    private fun loadAd(args: HashMap<String, Any>): Boolean {
        val id = args["id"] as Int
        val placementId = args["placementId"] as String

        var interstitialAd = adsById[id]
        if (interstitialAd == null) {
            interstitialAd = InterstitialAd(activity ?: context, placementId)
            adsById[id] = interstitialAd
            idsByAd[interstitialAd] = id
        }

        try {
            if (!interstitialAd.isAdLoaded) {
                val loadAdConfig = interstitialAd.buildLoadAdConfig()
                    .withAdListener(this)
                    .build()
                interstitialAd.loadAd(loadAdConfig)
            }
        } catch (e: Exception) {
            return false
        }
        return true
    }

    private fun showAd(args: HashMap<String, Any>): Boolean {
        val id = args["id"] as Int
        val delay = args["delay"] as? Int ?: 0
        val interstitialAd = adsById[id]

        if (interstitialAd == null || !interstitialAd.isAdLoaded) {
            return false
        }

        if (interstitialAd.isAdInvalidated) {
            return false
        }

        if (delay <= 0) {
            interstitialAd.show()
        } else {
            delayHandler.postDelayed({
                if (interstitialAd.isAdLoaded && !interstitialAd.isAdInvalidated) {
                    interstitialAd.show()
                }
            }, delay.toLong())
        }
        return true
    }

    private fun destroyAd(args: HashMap<String, Any>): Boolean {
        val id = args["id"] as Int
        val interstitialAd = adsById[id] ?: return false

        interstitialAd.destroy()
        adsById.remove(id)
        idsByAd.remove(interstitialAd)
        return true
    }

    // InterstitialAdListener methods
    override fun onInterstitialDisplayed(ad: Ad?) {
        val id = idsByAd[ad] ?: return
        val args = hashMapOf<String, Any?>(
            "id" to id,
            "placement_id" to ad?.placementId,
            "invalidated" to ad?.isAdInvalidated
        )
        channel.invokeMethod(Constants.DISPLAYED_METHOD, args)
    }

    override fun onInterstitialDismissed(ad: Ad?) {
        val id = idsByAd[ad] ?: return
        val args = hashMapOf<String, Any?>(
            "id" to id,
            "placement_id" to ad?.placementId,
            "invalidated" to ad?.isAdInvalidated
        )
        channel.invokeMethod(Constants.DISMISSED_METHOD, args)
    }

    override fun onError(ad: Ad?, adError: AdError?) {
        val id = idsByAd[ad] ?: return
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
        val id = idsByAd[ad] ?: return
        val args = hashMapOf<String, Any?>(
            "id" to id,
            "placement_id" to ad?.placementId,
            "invalidated" to ad?.isAdInvalidated
        )
        channel.invokeMethod(Constants.LOADED_METHOD, args)
    }

    override fun onAdClicked(ad: Ad?) {
        val id = idsByAd[ad] ?: return
        val args = hashMapOf<String, Any?>(
            "id" to id,
            "placement_id" to ad?.placementId,
            "invalidated" to ad?.isAdInvalidated
        )
        channel.invokeMethod(Constants.CLICKED_METHOD, args)
    }

    override fun onLoggingImpression(ad: Ad?) {
        val id = idsByAd[ad] ?: return
        val args = hashMapOf<String, Any?>(
            "id" to id,
            "placement_id" to ad?.placementId,
            "invalidated" to ad?.isAdInvalidated
        )
        channel.invokeMethod(Constants.LOGGING_IMPRESSION_METHOD, args)
    }
}
