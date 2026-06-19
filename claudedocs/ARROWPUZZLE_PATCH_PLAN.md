# Arrow Puzzle (Easybrain) Patch Plan

**Target:** `com.easybrain.arrow.puzzle.game` v1.7.0 (versionCode 10700026)
**APK:** `APK/com.easybrain.arrow.puzzle.game_s25ultra.apk` (single universal apk, 160 MB; arm64; minSdk 26; targetSdk 35)
**Source namespace:** `app.chiggi.arrowpuzzle`
**Engine:** Unity IL2CPP (`libil2cpp.so`/`libunity.so`) — game logic native, but ads + analytics run on Android/DEX (Unity↔Java via JNI). DEX NOT packed.

## Approved scope (user, 2026-06-19)
1. **Remove ads** — ALL types (banner + interstitial + rewarded). User accepted losing watch-to-earn rewards.
2. **Disable analytics** — safe depth: stop event upload + remove AD_ID (keep SDK init).
3. **Branding** — app name + package → morphe variant.

## Hooks (verified)
| Patch | Hook | Change |
|---|---|---|
| Remove ads | `com.easybrain.ads.unity.AdsPlugin` (named JNI facade): `IsInterstitialCached/Ready`, `ShowInterstitial`, `IsRewardedCached`, `ShowRewarded` → `return false`; `ShowBanner` x2, `EnableBanner` → `return-void`; `GetBannerHeight` x2 → `return 0` | 10 methods; named (JNI contract, not obfuscated) |
| Disable analytics | `AnalyticsPlugin.AnalyticsSendEvent(String,String)` (named) + `xb.i.j(event)` (central collector, obfuscated) → `return-void` | init untouched |
| Remove AD_ID | strip `AD_ID` + `ACCESS_ADSERVICES_AD_ID/ATTRIBUTION/TOPICS` perms | resource |
| Change app name | label → "Arrow Puzzle Morphe" (launcher `com.easybrain.template.SplashActivity`) | resource |
| Change package name | → `com.easybrain.arrow.puzzle.game.morphe` | resource (finalize) |

## Status (2026-06-19)
- ✅ Built `patches-1.11.0.mpp`; all 5 patches `Applied` (Remove ads = 10 AdsPlugin fingerprints resolved).
- ✅ Verified (patch with each-patch defaults, NO shared options file): package `com.easybrain.arrow.puzzle.game.morphe`, label `Arrow Puzzle Morphe`, AD_ID/adservices removed; baksmali confirms all 10 ad methods + both analytics methods return false/0/void.
- ✅ Multidex: AdsPlugin/AnalyticsPlugin were duplicated in classes4+classes14 in the input; rebuild de-duped to one copy (patched).
- ⏳ NOT pushed; NOT device-tested.

## Risks / caveats
- ⚠️ **Native anti-tamper:** `libsigner.so` + `libpglarmor.so` may run a native integrity/signature check; re-signing could trigger crash/close-on-launch or ad-fallback. Unverifiable without device. If it crashes on launch, a native lib check is firing (would need lib neutralization — out of scope for smali).
- ⚠️ **CLI options-create collision (cosmetic, Manager unaffected):** `morphe-cli options-create` flattens same-named patches ("Change app name"/"Change package name" exist for 4 apps) into ONE option entry → wrong branding if you pass that shared options file. Fix when CLI-patching: omit `--options-file` (each patch uses its own default) or set the value explicitly. Morphe Manager scopes options per app, so end users are unaffected.
- ⚠️ Rewarded ads removed → no watch-to-earn rewards (user's explicit choice).
- `xb.i.j` analytics hook is obfuscated → re-fingerprint on app update (AdsPlugin/AnalyticsPlugin names are stable).
- `_s25ultra` filename: this apk may already be a 3rd-party mod; patched whatever this build is.

## Files
```
patches/src/main/kotlin/app/chiggi/arrowpuzzle/patches/
  shared/Constants.kt
  ads/{Fingerprints,RemoveAdsPatch}.kt
  misc/analytics/{Fingerprints,DisableAnalyticsPatch,RemoveAdIdPatch}.kt
  misc/branding/ChangeAppNamePatch.kt
  misc/packagename/ChangePackageNamePatch.kt
```
Output (gitignored): `arrowpuzzle/output/arrowpuzzle_morphe_patched.apk`
