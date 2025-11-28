package com.dsi.easy_audience_network

import android.content.Context
import android.view.View
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.AdListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class BannerAdFactory(private val messenger: BinaryMessenger) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        return BannerAdView(context, viewId, args as HashMap<String, Any>, messenger)
    }
}

class BannerAdView(
    context: Context,
    viewId: Int,
    args: HashMap<String, Any>,
    messenger: BinaryMessenger
) : PlatformView, AdListener {

    private val adView: AdView
    private val channel: MethodChannel

    init {
        channel = MethodChannel(messenger, "${Constants.BANNER_AD_CHANNEL}_$viewId")
        
        val placementId = args["id"] as String
        val adSize = getBannerSize(args)
        
        adView = AdView(context, placementId, adSize)
        
        val loadAdConfig = adView.buildLoadAdConfig()
            .withAdListener(this)
            .build()
            
        adView.loadAd(loadAdConfig)
    }

    private fun getBannerSize(args: HashMap<String, Any>): AdSize {
        val height = args["height"] as Int
        
        return when {
            height >= 250 -> AdSize.RECTANGLE_HEIGHT_250
            height >= 90 -> AdSize.BANNER_HEIGHT_90
            else -> AdSize.BANNER_HEIGHT_50
        }
    }

    override fun getView(): View {
        return adView
    }

    override fun dispose() {
        adView.destroy()
    }

    override fun onError(ad: Ad?, adError: AdError?) {
        val args = hashMapOf<String, Any?>(
            "placement_id" to ad?.placementId,
            "invalidated" to ad?.isAdInvalidated,
            "error_code" to adError?.errorCode,
            "error_message" to adError?.errorMessage
        )
        channel.invokeMethod(Constants.ERROR_METHOD, args)
    }

    override fun onAdLoaded(ad: Ad?) {
        val args = hashMapOf<String, Any?>(
            "placement_id" to ad?.placementId,
            "invalidated" to ad?.isAdInvalidated
        )
        channel.invokeMethod(Constants.LOADED_METHOD, args)
    }

    override fun onAdClicked(ad: Ad?) {
        val args = hashMapOf<String, Any?>(
            "placement_id" to ad?.placementId,
            "invalidated" to ad?.isAdInvalidated
        )
        channel.invokeMethod(Constants.CLICKED_METHOD, args)
    }

    override fun onLoggingImpression(ad: Ad?) {
        val args = hashMapOf<String, Any?>(
            "placement_id" to ad?.placementId,
            "invalidated" to ad?.isAdInvalidated
        )
        channel.invokeMethod(Constants.LOGGING_IMPRESSION_METHOD, args)
    }
}
