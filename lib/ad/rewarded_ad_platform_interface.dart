import 'package:flutter/services.dart';

import '../constants.dart';

enum RewardedAdPlatformInterfaceResult {
  /// Rewarded video error.
  ERROR,

  /// Rewarded video loaded successfully.
  LOADED,

  /// Rewarded video clicked.
  CLICKED,

  /// Rewarded video impression logged.
  LOGGING_IMPRESSION,

  /// Rewarded video played till the end. Use it to reward the user.
  VIDEO_COMPLETE,

  /// Rewarded video closed.
  VIDEO_CLOSED,
}

class RewardedAdPlatformInterface {
  static final _listeners =
      <int, void Function(RewardedAdPlatformInterfaceResult, dynamic)>{};

  static const _channel = MethodChannel(REWARDED_VIDEO_CHANNEL);

  /// Loads a rewarded video Ad in background. Replace the default [placementId]
  /// with the one which you obtain by signing-up for Facebook Audience Network.
  ///
  /// [listener] passes [RewardedAdPlatformInterfaceResult] and information associated with
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
  static Future<bool?> loadRewardedVideoAd(
    int id, {
    String placementId = "YOUR_PLACEMENT_ID",
    String? userId,
    required Function(RewardedAdPlatformInterfaceResult, dynamic) listener,
  }) async {
    try {
      final args = <String, dynamic>{
        "id": id,
        "placementId": placementId,
        "userId": userId,
      };

      final result = await _channel.invokeMethod(
        LOAD_REWARDED_VIDEO_METHOD,
        args,
      );
      _channel.setMethodCallHandler(_rewardedMethodCall);
      _listeners[id] = listener;

      return result;
    } on PlatformException {
      return false;
    }
  }

  /// Shows a rewarded video Ad after it has been loaded. (This needs to be
  /// called only after calling [loadRewardedVideoAd] function). [delay] is in
  /// milliseconds.
  ///
  /// Example:
  ///
  /// ```dart
  /// RewardedAdPlatformInterface.loadRewardedVideoAd(
  ///   listener: (result, value) {
  ///     if(result == RewardedVideoAdResult.LOADED)
  ///       RewardedAdPlatformInterface.showRewardedVideoAd();
  ///   },
  /// );
  /// ```
  static Future<bool?> showRewardedVideoAd(int id, {int delay = 0}) async {
    try {
      final args = <String, dynamic>{
        "id": id,
        "delay": delay,
      };

      final result = await _channel.invokeMethod(
        SHOW_REWARDED_VIDEO_METHOD,
        args,
      );

      return result;
    } on PlatformException {
      return false;
    }
  }

  /// Removes the rewarded video Ad.
  static Future<bool?> destroyRewardedVideoAd(int id) async {
    try {
      final args = <String, dynamic>{
        "id": id,
      };

      final result = await _channel.invokeMethod(
        DESTROY_REWARDED_VIDEO_METHOD,
        args,
      );
      _listeners.remove(id);
      return result;
    } on PlatformException {
      return false;
    }
  }

  static Future<dynamic> _rewardedMethodCall(MethodCall call) async {
    final id = call.arguments['id'];
    final listener = _listeners[id];
    assert(listener != null);
    if (listener == null) return;

    switch (call.method) {
      case REWARDED_VIDEO_COMPLETE_METHOD:
        listener(
            RewardedAdPlatformInterfaceResult.VIDEO_COMPLETE, call.arguments);
        break;
      case REWARDED_VIDEO_CLOSED_METHOD:
        listener(
            RewardedAdPlatformInterfaceResult.VIDEO_CLOSED, call.arguments);
        break;
      case ERROR_METHOD:
        listener(RewardedAdPlatformInterfaceResult.ERROR, call.arguments);
        break;
      case LOADED_METHOD:
        listener(RewardedAdPlatformInterfaceResult.LOADED, call.arguments);
        break;
      case CLICKED_METHOD:
        listener(RewardedAdPlatformInterfaceResult.CLICKED, call.arguments);
        break;
      case LOGGING_IMPRESSION_METHOD:
        listener(RewardedAdPlatformInterfaceResult.LOGGING_IMPRESSION,
            call.arguments);
        break;
    }
  }
}
