# CrazyGames Patch Plan (com.crazygames.crazygamesapp)

Status: IMPLEMENTING. Version bump 1.13.1 -> 1.14.0.

## App facts (verified via apktool/baksmali)
- Package `com.crazygames.crazygamesapp`, versionName `1.6.7`, versionCode `287`, minSdk `24`, targetSdk `36`.
- Distributed as a single universal APK -> `ApkFileType.APK`.
- Architecture: **Capacitor (Ionic) WebView shell** around the CrazyGames Next.js portal (`app.crazygames.com`). Bundled static export in `assets/public/`; `@capawesome/capacitor-live-update` can hot-swap the bundle at runtime.
- Launcher activity: `com.crazygames.crazygamesapp.MainActivity`. App label string `app_name`/`title_activity_main` = "CrazyGames".

## Ad stack
| Layer | Mechanism | DEX-patchable |
|---|---|---|
| Native interstitial/rewarded/banner | `@crazygames/capacitor-admob` -> `com.getcapacitor.community.admob.AdMob` (plain DEX, class name NOT obfuscated) | YES |
| AdMob mediation: Facebook Audience Network | `assets/audience_network/classes.dex` | killed at AdMob entry |
| AdMob mediation: Pangle/TikTok 7.9.1.0 | `libpgl*.so`, `libnms.so`, `libpglarmor.so`, `libsigner.so` (native, armored) | killed at AdMob entry |
| In-page web video ads | served remotely inside the WebView from crazygames.com | **NO** (not in DEX) |

## Obfuscation map (R8, version-pinned to 1.6.7)
- `Lcom/getcapacitor/community/admob/AdMob;` = AdMob plugin (NOT obfuscated). Method names kept (Capacitor invokes by reflection): `showInterstitial`, `prepareInterstitial`, `showBanner`, `resumeBanner`, `showRewardVideoAd`, `showRewardInterstitialAd`, `prepareRewardVideoAd`, `prepareRewardInterstitialAd`, `initialize`, `hideBanner`, `removeBanner`, consent/tracking methods.
- `Lcom/getcapacitor/Y;` = `PluginCall`. `Y->z()V` = `resolve()` (empty success). `Y->A(Lcom/getcapacitor/L;)V` = `resolve(JSObject)`.
- `Lcom/getcapacitor/L;` = `JSObject` (extends org.json.JSONObject). `L->m(String,String)L` = putString, `L->j(String,int)L` = putInt. Framework `JSONObject.put(String,Object)` is also available.
- `Lcom/getcapacitor/X;` = `Plugin` base. `notifyListeners(Ljava/lang/String;Lcom/getcapacitor/L;)V` = name KEPT.
- Reward delivery (native, when ad watched): build JSObject{type,amount} -> fire listener event `onRewardedVideoAdReward` -> `PluginCall.resolve(obj)`. Web grants reward on the listener event (client-side; no SSV gate found in web bundle — the "ssv" hits were Norwegian UI strings).

## Patches (all default = true)
1. **Remove ads** (bytecodePatch, `ads/RemoveAdsPatch.kt`)
   - resolve-empty (raw smali `invoke-virtual {p1}, Lcom/getcapacitor/Y;->z()V` + `return-void`): `showInterstitial`, `prepareInterstitial`, `showBanner`, `resumeBanner`, `prepareRewardVideoAd`, `prepareRewardInterstitialAd`. Resolving (not bare return) so the web `await` never hangs.
   - auto-grant (raw smali `const-string vN,<event>` + `invoke-static {p0,p1,vN}` -> extension `grantReward`): `showRewardVideoAd` (event `onRewardedVideoAdReward`), `showRewardInterstitialAd` (event `onRewardedInterstitialAdReward`). Grants the reward WITHOUT showing the ad.
   - Left intact: `initialize`, `hideBanner`, `removeBanner`, consent/tracking/volume methods (web state-machine stability).
2. **Disable analytics** (bytecodePatch, `misc/analytics/DisableAnalyticsPatch.kt`)
   - `io.sentry.capacitor.SentryCapacitor.initNativeSdk(PluginCall)` -> resolve empty. Stops native Sentry crash/telemetry upload. Firebase auth/ConfigCat/SuprSend left intact (login + flags + push keep working).
3. **Remove AD_ID permission** (resourcePatch, `misc/analytics/RemoveAdIdPatch.kt`)
   - strip `com.google.android.gms.permission.AD_ID`, `ACCESS_ADSERVICES_AD_ID`, `ACCESS_ADSERVICES_ATTRIBUTION`, `ACCESS_ADSERVICES_TOPICS`, `ACCESS_ADSERVICES_CUSTOM_AUDIENCE`.
4. **Change app name** (resourcePatch, `misc/branding/ChangeAppNamePatch.kt`)
   - `<application android:label>` + launcher `MainActivity` label -> stringOption default "CrazyGames Morphe".

NOT included (user decisions): no package rename (keeps Google/Facebook/Apple login working); no LiveUpdate disable.

## Extension
`extensions/extension/src/main/java/app/chiggi/crazygames/extension/AdRewardPatch.java`
- `grantReward(Object plugin, Object call, String event)`: walk plugin class hierarchy to find kept `notifyListeners(String, JSObject)`; derive JSObject class from its 2nd param type; build reward via framework `JSONObject.put("type","rewarded")` + `put("amount",1)`; fire `notifyListeners(event, reward)`; resolve the call with the reward (first public `void(JSObject)` method). All wrapped in try/catch -> graceful no-op on any mismatch. Register-safe injection (`invoke-static` uses only p0/p1/one const-string temp).

## Compatibility constants
- name "CrazyGames", packageName `com.crazygames.crazygamesapp`, `ApkFileType.APK`, appIconColor `0x0C0D14` (real `ic_launcher_background`), target 1.6.7 / minSdk 24.

## Known gotchas / risks (must device-test)
- **Pangle armor/signer** (`libpglarmor.so`,`libsigner.so`) = ByteDance anti-tamper. Re-signed APK may trip it: best case Pangle silently no-fills (good), worst case crash on launch. Untested.
- **Web in-page video ads** (pre/mid-roll inside a game) come from crazygames.com servers, NOT the native SDK -> NOT removable by DEX. "Remove ads" kills native interstitial/banner/rewarded only -> partial.
- **Auto-reward** depends on the web granting on the `onReward...Reward` listener client-side. If CrazyGames added server verification, the fake event grants nothing (no crash, just no reward).
- Obfuscated anchors (`Y`,`L`,`z`,`A`) are version-specific -> pinned to 1.6.7.

## Build/verify
- bump gradle.properties -> 1.14.0; update patches/build.gradle.kts description (add CrazyGames).
- `./gradlew :extensions:extension:build buildAndroid generatePatchesList` (delete old patches/build/libs/*.mpp first).
- apply via morphe-cli WITHOUT --options-file (per-patch defaults; avoids the options-name-collision branding leak).
- baksmali the output: confirm z()/grantReward injected into the right AdMob methods, Sentry initNativeSdk neutered, perms stripped, label changed.
- update README, patches-bundle.json (1.14.0), Talk.md, learn.md, Table_of_Contents.md. Await user approval before push/release.
