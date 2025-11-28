import 'package:flutter/services.dart';

import '../constants.dart';

enum InterstitialAdPlatformInterfaceResult {
  /// Interstitial Ad displayed to the user
  DISPLAYED,

  /// Interstitial Ad dismissed by the user
  DISMISSED,

  /// Interstitial Ad error
  ERROR,

  /// Interstitial Ad loaded
  LOADED,

  /// Interstitial Ad clicked
  CLICKED,

  /// Interstitial Ad impression logged
  LOGGING_IMPRESSION,
}

class InterstitialAdPlatformInterface {
  static final _listeners =
      <int, void Function(InterstitialAdPlatformInterfaceResult, dynamic)>{};

  static const _channel = MethodChannel(INTERSTITIAL_AD_CHANNEL);

  /// Loads an Interstitial Ad in background. Replace the default [placementId]
  /// with the one which you obtain by signing-up for Facebook Audience Network.
  ///
  /// [listener] passes [InterstitialAdPlatformInterfaceResult] and information associated with
  /// the result to the implemented callback.
  ///
  /// Information will generally be of type Map with details such as:
  ///
  /// ```dart
  /// {
  ///   'placement\_id': "YOUR\_PLACEMENT\_ID",
  ///   'invalidated': false,
  ///   'error\_code': 2,
  ///   'error\_message': "No internet connection",
  /// }
  /// ```
  static Future<bool?> loadInterstitialAd(
    int id, {
    String placementId = "YOUR_PLACEMENT_ID",
    required Function(InterstitialAdPlatformInterfaceResult, dynamic) listener,
  }) async {
    try {
      final args = <String, dynamic>{
        "id": id,
        "placementId": placementId,
      };

      final result = await _channel.invokeMethod(
        LOAD_INTERSTITIAL_METHOD,
        args,
      );
      _channel.setMethodCallHandler(_interstitialMethodCall);
      _listeners[id] = listener;

      return result;
    } on PlatformException {
      return false;
    }
  }

  /// Shows an Interstitial Ad after it has been loaded. (This needs to be called
  /// only after calling [loadInterstitialAd] function). [delay] is in
  /// milliseconds.
  ///
  /// Example:
  ///
  /// ```dart
  /// InterstitialAdPlatformInterface.loadInterstitialAd(
  ///   listener: (result, value) {
  ///     if (result == InterstitialAdResult.LOADED)
  ///       InterstitialAdPlatformInterface.showInterstitialAd(delay: 5000);
  ///   },
  /// );
  /// ```
  static Future<bool?> showInterstitialAd(int id, {int? delay = 0}) async {
    try {
      final args = <String, dynamic>{
        "id": id,
        "delay": delay,
      };

      final result = await _channel.invokeMethod(
        SHOW_INTERSTITIAL_METHOD,
        args,
      );

      return result;
    } on PlatformException {
      return false;
    }
  }

  /// Removes the Ad.
  static Future<bool?> destroyInterstitialAd(int id) async {
    try {
      final args = <String, dynamic>{
        "id": id,
      };

      final result = await _channel.invokeMethod(
        DESTROY_INTERSTITIAL_METHOD,
        args,
      );
      _listeners.remove(id);
      return result;
    } on PlatformException {
      return false;
    }
  }

  static Future<dynamic> _interstitialMethodCall(MethodCall call) async {
    final id = call.arguments['id'];
    final listener = _listeners[id];
    assert(listener != null);
    if (listener == null) return;

    switch (call.method) {
      case DISPLAYED_METHOD:
        listener(
            InterstitialAdPlatformInterfaceResult.DISPLAYED, call.arguments);
        break;
      case DISMISSED_METHOD:
        listener(
            InterstitialAdPlatformInterfaceResult.DISMISSED, call.arguments);
        break;
      case ERROR_METHOD:
        listener(InterstitialAdPlatformInterfaceResult.ERROR, call.arguments);
        break;
      case LOADED_METHOD:
        listener(InterstitialAdPlatformInterfaceResult.LOADED, call.arguments);
        break;
      case CLICKED_METHOD:
        listener(InterstitialAdPlatformInterfaceResult.CLICKED, call.arguments);
        break;
      case LOGGING_IMPRESSION_METHOD:
        listener(InterstitialAdPlatformInterfaceResult.LOGGING_IMPRESSION,
            call.arguments);
        break;
    }
  }
}
