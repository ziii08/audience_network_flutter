# Easy Audience Network

A Flutter plugin for Facebook Audience Network (Meta Audience Network) supporting Android (Kotlin) and iOS (Swift).

> **Note**: This package is a modern replacement for `audience_network_flutter`.

## Features

- **Banner Ads**: Standard, Large, and Medium Rectangle sizes.
- **Interstitial Ads**: Full-screen ads.
- **Rewarded Video Ads**: Video ads that reward users.
- **Native Implementation**: Uses Kotlin for Android and Swift for iOS.
- **Latest SDK**: Supports Meta Audience Network SDK 6.21.0.

## Installation

Add `easy_audience_network` to your `pubspec.yaml`:

```yaml
dependencies:
  easy_audience_network:
    path: packages/easy_audience_network
```

## Usage

### Initialization

Initialize the SDK before using any ads.

```dart
import 'package:easy_audience_network/easy_audience_network.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await EasyAudienceNetwork.init(
    testingId: "YOUR_TESTING_ID", // Optional, check logs for your device ID
    testMode: true, // Set to false for production
  );
  runApp(MyApp());
}
```

### Banner Ads

```dart
BannerAd(
  placementId: "YOUR_PLACEMENT_ID",
  bannerSize: BannerSize.STANDARD,
  listener: BannerAdListener(
    onLoaded: () => print("Banner loaded"),
    onError: (code, message) => print("Banner error: $message"),
    onClicked: () => print("Banner clicked"),
  ),
)
```

### Interstitial Ads

```dart
final interstitialAd = InterstitialAd("YOUR_PLACEMENT_ID");

interstitialAd.listener = InterstitialAdListener(
  onLoaded: () {
    print("Interstitial loaded");
    interstitialAd.show();
  },
  onDismissed: () {
    print("Interstitial dismissed");
    interstitialAd.destroy();
  },
  onError: (code, message) {
    print("Interstitial error: $message");
  },
);

interstitialAd.load();
```

### Rewarded Ads

```dart
final rewardedAd = RewardedAd("YOUR_PLACEMENT_ID", userId: "USER_ID");

rewardedAd.listener = RewardedAdListener(
  onLoaded: () {
    print("Rewarded loaded");
    rewardedAd.show();
  },
  onVideoComplete: () {
    print("User earned reward");
  },
  onVideoClosed: () {
    print("Rewarded closed");
    rewardedAd.destroy();
  },
  onError: (code, message) {
    print("Rewarded error: $message");
  },
);

rewardedAd.load();
```

## Migration

If you are migrating from `audience_network_flutter`, please see the [Migration Guide](MIGRATION_GUIDE.md).
