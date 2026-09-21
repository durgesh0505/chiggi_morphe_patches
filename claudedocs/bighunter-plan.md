# Big Hunter (com.kakarod.bighunter) — patch plan

Slug: `bighunter`. Input: `workspace/input/bighunter/bighunter.apk` (v3.1.1, versionCode 72, arm64-only, targetSdk 36, 120MB). Output: `workspace/output/bighunter/bighunter-morphe.apk`.

## Goal (operator, 2026-09-21)
1. Remove ads.
2. Free / unlimited in-app purchases.

## Engine / SDK map (from aapt + zip listing)
- Native game engine: `lib/arm64-v8a/libMyGame.so` (13.7MB) — custom C++ (Cocos2d-x-style). Game logic + likely IAP grant live here.
- Java layer: 4 dex (`classes.dex`..`classes4.dex`) = SDK wrappers + JNI bridge.
- Ads: **AppLovin MAX** (`libapplovin-native-crash-reporter.so`, `applovin_*` resources) + **AdMob** mediation (`admob_*` resources). Ad show is Java-side, triggered by native via JNI.
- IAP: `com.android.vending.BILLING` → Google Play Billing (Java `BillingClient`), purchase result likely bridged back to native.

## Investigation (before any patch — RULEBOOK: verify a client gate exists)
1. ADS: find the Java ad-show entry points (AppLovin MAX `MaxInterstitialAd.showAd()`, `MaxRewardedAd.showAd()`, banner attach, `AppLovinSdk.initializeSdk`). Confirm the native→Java JNI ad methods. Neuter show calls / force ad callbacks to "dismissed/rewarded" so rewarded-ad gates still grant.
2. IAP: find the billing wrapper. Determine WHERE a purchase is granted:
   - Java `BillingClient` `PurchasesUpdatedListener` / `queryPurchases` → does a Java method decide entitlement, or does it call `nativeOnPurchase*` into libMyGame.so?
   - Is there SERVER receipt validation? (grep for verify endpoints.) If the grant is server-validated or decided inside libMyGame.so, it is NOT bytecode-patchable — say so.
3. Report split verdict per RULEBOOK native-game IAP rule; caveat native re-verify.

## Method
- jadx decompile → `/tmp/bh_work/jadx` (scratch; not repo).
- Delegate the ad + billing trace to a subagent (fresh) with FILE:LINE evidence, then verify key claims.
- Patches under `patches/src/main/kotlin/app/chiggi/bighunter/`, extension (if needed) under `extensions/.../bighunter/`.

## Risks / gotchas
- Rewarded ads often GATE a reward (coins/revive). Neutering the ad must still fire the reward callback, else the game soft-locks waiting for an ad. Force onUserRewarded + onAdHidden, not just block show.
- If IAP grant is native (libMyGame.so) or server-validated → free IAP is NOT DEX-patchable; will report unpatchable rather than fake it.
- Re-signed game + Play Billing: real purchases won't work anyway on a re-signed app; "free IAP" only makes sense if the grant path is client-side.

## Build
`./gradlew :patches:build` → morphe-cli patch with `--keystore Morphe.keystore` (default alias/pw, no password flag) → verify with dexdump.

## STATUS: COMPLETE (2026-09-21 07:26:58 PM)
Both patches built, applied, dexdump-verified. Output at workspace/output/bighunter/bighunter-morphe.apk. Awaiting operator decision on commit+release.
