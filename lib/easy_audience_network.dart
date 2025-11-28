/// Facebook Audience Network plugin for Flutter applications.
///
/// This library uses native API of [Facebook Audience Network](https://developers.facebook.com/docs/audience-network)
/// to provide functionality for Flutter applications.
///
/// This is a modern rewrite using Kotlin for Android and Swift for iOS.
library;

import 'package:flutter/services.dart';

import 'constants.dart';

export 'ad/banner_ad.dart';
export 'ad/interstitial_ad.dart';
export 'ad/rewarded_ad.dart';

/// All non-widget functions such as initialization, loading interstitial,
/// and rewarded video ads are enclosed in this class.
///
/// Initialize the Facebook Audience Network by calling the static [init]
/// function before using any ads.
class EasyAudienceNetwork {
  static const _channel = MethodChannel(MAIN_CHANNEL);

  /// Initializes the Facebook Audience Network. [testingId] can be used to
  /// obtain test Ads. [testMode] can be used to obtain test Ads as well,
  /// it is more useful on iOS where testingId keeps changing.
  ///
  /// [testingId] can be obtained by running the app once without the testingId.
  /// Check the log to obtain the [testingId] for your device.
  ///
  /// Example:
  /// ```dart
  /// await EasyAudienceNetwork.init(
  ///   testingId: "YOUR_TESTING_ID", // Optional
  ///   testMode: true, // Set to false for production
  /// );
  /// ```
  static Future<bool?> init({
    String? testingId,
    bool testMode = false,
    bool iOSAdvertiserTrackingEnabled = false,
  }) async {
    Map<String, dynamic> initValues = {
      "testingId": testingId,
      "iOSAdvertiserTrackingEnabled": iOSAdvertiserTrackingEnabled,
      "testMode": testMode,
    };

    try {
      final result = await _channel.invokeMethod(INIT_METHOD, initValues);
      return result;
    } on PlatformException {
      return false;
    }
  }
}
