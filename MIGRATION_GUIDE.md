# Migration Guide: audience_network_flutter → easy_audience_network

This guide helps you migrate from the deprecated `audience_network_flutter` package to the new `easy_audience_network` package.

## Key Changes

- **Android Implementation**: Switched from Java to Kotlin.
- **SDK Version**: Updated to Meta Audience Network SDK 6.21.0.
- **Package Name**: Changed from `audience_network_flutter` to `easy_audience_network`.
- **API Structure**: More consistent and Dart-idiomatic API.

## Dependency Update

Remove the old package and add the new one in your `pubspec.yaml`:

```yaml
dependencies:
  # Remove this
  # audience_network_flutter: ^x.x.x
  
  # Add this
  easy_audience_network:
    path: packages/easy_audience_network
```

## Initialization

**Old:**
```dart
import 'package:audience_network_flutter/audience_network_flutter.dart';

void main() {
  AudienceNetwork.init(
    testingId: "YOUR_TESTING_ID",
    testMode: true,
  );
}
```

**New:**
```dart
import 'package:easy_audience_network/easy_audience_network.dart';

void main() async {
  await EasyAudienceNetwork.init(
    testingId: "YOUR_TESTING_ID",
    testMode: true,
  );
}
```

## Banner Ads

**Old:**
```dart
BannerAd(
  placementId: "YOUR_PLACEMENT_ID",
  bannerSize: BannerSize.STANDARD,
  listener: BannerAdListener(
    onError: (code, message) => print("Error: $message"),
    onLoaded: () => print("Loaded"),
    onClicked: () => print("Clicked"),
    onLoggingImpression: () => print("Logging Impression"),
  ),
)
```

**New:**
```dart
BannerAd(
  placementId: "YOUR_PLACEMENT_ID",
  bannerSize: BannerSize.STANDARD,
  listener: BannerAdListener(
    onError: (code, message) => print("Error: $message"),
    onLoaded: () => print("Loaded"),
    onClicked: () => print("Clicked"),
    onLoggingImpression: () => print("Logging Impression"),
  ),
  keepAlive: true, // Optional, keeps ad alive in scroll views
)
```

## Interstitial Ads

**Old:**
```dart
final interstitialAd = InterstitialAd("YOUR_PLACEMENT_ID");
interstitialAd.listener = InterstitialAdListener(
  onLoaded: () {
    interstitialAd.show();
  },
  // ... other callbacks
);
interstitialAd.load();
```

**New:**
```dart
final interstitialAd = InterstitialAd("YOUR_PLACEMENT_ID");
interstitialAd.listener = InterstitialAdListener(
  onLoaded: () {
    interstitialAd.show();
  },
  onDismissed: () {
    interstitialAd.destroy();
  },
  onError: (code, message) {
    print("Error: $message");
  },
  // ... other callbacks
);
interstitialAd.load();
```

## Rewarded Ads

**Old:**
```dart
final rewardedAd = RewardedAd("YOUR_PLACEMENT_ID");
rewardedAd.listener = RewardedAdListener(
  onLoaded: () {
    rewardedAd.show();
  },
  onVideoComplete: () {
    print("Video Completed");
  },
  // ... other callbacks
);
rewardedAd.load();
```

**New:**
```dart
final rewardedAd = RewardedAd("YOUR_PLACEMENT_ID", userId: "USER_ID");
rewardedAd.listener = RewardedAdListener(
  onLoaded: () {
    rewardedAd.show();
  },
  onVideoComplete: () {
    print("Video Completed - Grant Reward");
  },
  onVideoClosed: () {
    rewardedAd.destroy();
  },
  onError: (code, message) {
    print("Error: $message");
  },
  // ... other callbacks
);
rewardedAd.load();
```

## Common Issues

### Android Build Failures
If you encounter build failures related to Android SDK versions, ensure your `android/app/build.gradle` has:
- `compileSdkVersion 34` (or higher)
- `minSdkVersion 21` (or higher)

### Ad Not Loading
- Check if `testMode` is enabled during development.
- Verify your `placementId` is correct.
- Ensure you have internet connection.
- Check the logs for specific error codes from the Audience Network SDK.
