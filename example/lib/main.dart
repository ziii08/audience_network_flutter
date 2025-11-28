import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:easy_audience_network/easy_audience_network.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await EasyAudienceNetwork.init(
    testMode: true, // Always true for example app
  );
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Easy Audience Network Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String _status = 'Idle';
  InterstitialAd? _interstitialAd;
  RewardedAd? _rewardedAd;

  void _updateStatus(String status) {
    setState(() {
      _status = status;
    });
    log(status);
  }

  void _loadInterstitial() {
    _updateStatus('Loading Interstitial...');
    _interstitialAd = InterstitialAd(InterstitialAd.testPlacementId);
    _interstitialAd!.listener = InterstitialAdListener(
      onLoaded: () {
        _updateStatus('Interstitial Loaded');
      },
      onDismissed: () {
        _updateStatus('Interstitial Dismissed');
        _interstitialAd!.destroy();
        _interstitialAd = null;
      },
      onError: (code, message) {
        _updateStatus('Interstitial Error: $code - $message');
      },
    );
    _interstitialAd!.load();
  }

  void _showInterstitial() {
    if (_interstitialAd != null) {
      _interstitialAd!.show();
    } else {
      _updateStatus('Interstitial not loaded');
    }
  }

  void _loadRewarded() {
    _updateStatus('Loading Rewarded...');
    _rewardedAd = RewardedAd(RewardedAd.testPlacementId, userId: "test_user");
    _rewardedAd!.listener = RewardedAdListener(
      onLoaded: () {
        _updateStatus('Rewarded Loaded');
      },
      onVideoComplete: () {
        _updateStatus('Rewarded Video Completed - Grant Reward!');
      },
      onVideoClosed: () {
        _updateStatus('Rewarded Closed');
        _rewardedAd!.destroy();
        _rewardedAd = null;
      },
      onError: (code, message) {
        _updateStatus('Rewarded Error: $code - $message');
      },
    );
    _rewardedAd!.load();
  }

  void _showRewarded() {
    if (_rewardedAd != null) {
      _rewardedAd!.show();
    } else {
      _updateStatus('Rewarded not loaded');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Easy Audience Network'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              color: Colors.grey[200],
              child: Text('Status: $_status', textAlign: TextAlign.center),
            ),
            const SizedBox(height: 20),
            const Padding(
              padding: EdgeInsets.all(8.0),
              child: Text('Banner Ad', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            ),
            Container(
              alignment: Alignment.center,
              child: BannerAd(
                placementId: BannerAd.testPlacementId,
                bannerSize: BannerSize.STANDARD,
                listener: BannerAdListener(
                  onLoaded: () => _updateStatus('Banner Loaded'),
                  onError: (code, msg) => _updateStatus('Banner Error: $msg'),
                  onClicked: () => _updateStatus('Banner Clicked'),
                ),
              ),
            ),
            // Container(
            //   alignment: Alignment.center,
            //   child: BannerAd(
            //     placementId: BannerAd.testPlacementId,
            //     bannerSize: BannerSize.LARGE,
            //     listener: BannerAdListener(
            //       onLoaded: () => _updateStatus('Banner Loaded'),
            //       onError: (code, msg) => _updateStatus('Banner Error: $msg'),
            //       onClicked: () => _updateStatus('Banner Clicked'),
            //     ),
            //   ),
            // ),
            // Container(
            //   alignment: Alignment.center,
            //   child: BannerAd(
            //     placementId: BannerAd.testPlacementId,
            //     bannerSize: BannerSize.MEDIUM_RECTANGLE,
            //     listener: BannerAdListener(
            //       onLoaded: () => _updateStatus('Banner Loaded'),
            //       onError: (code, msg) => _updateStatus('Banner Error: $msg'),
            //       onClicked: () => _updateStatus('Banner Clicked'),
            //     ),
            //   ),
            // ),
            const SizedBox(height: 20),
            const Divider(),
            const Padding(
              padding: EdgeInsets.all(8.0),
              child: Text('Interstitial Ad', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton(
                  onPressed: _loadInterstitial,
                  child: const Text('Load Interstitial'),
                ),
                ElevatedButton(
                  onPressed: _showInterstitial,
                  child: const Text('Show Interstitial'),
                ),
              ],
            ),
            const SizedBox(height: 20),
            const Divider(),
            const Padding(
              padding: EdgeInsets.all(8.0),
              child: Text('Rewarded Ad', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            ),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                ElevatedButton(
                  onPressed: _loadRewarded,
                  child: const Text('Load Rewarded'),
                ),
                ElevatedButton(
                  onPressed: _showRewarded,
                  child: const Text('Show Rewarded'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
