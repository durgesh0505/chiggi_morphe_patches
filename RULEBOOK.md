# RULEBOOK — chiggi_morphe_patches

Project law. Read before any change. Overrides defaults. Rules earn a place only after an incident cost us something or the operator demanded it. One line each. Why = the actual incident.

## Universal

| Rule | Why |
|------|-----|
| Re-signing + VPN/proxy + client spoofs on a REAL streaming/social/paid account = ban risk; recommend throwaway account only, official app for the real one — say it early. | JioHotstar re-signed build + VPN got the operator's real account locked 24h "malicious activity" (NM-4290). |
| Before writing a bypass for a server "verdict", grep for the client enum/branch that READS it; if the payload is opaque (ByteString/blob) and only the network layer touches it, there is nothing to bypass. | JioHotstar `ProxyState` is an opaque server-minted ByteString (ttlSec/issueAt/num/data); no NO_PROXY/BLOCKED enum exists client-side — the "bypass" was fictional. |
| Never neuter a stateful echo/ACK protocol; if the client stores a server value and echoes it back on later requests, dropping it self-reports tamper on EVERY request. | `proxyStateBypassPatch` returned Unit in `ProxyStateInterceptor$intercept$2$1`, so the client echoed a clean proxy-state forever while the server said BLOCKED → traced as the "malicious activity" account block. |
| AD_ID / BILLING permission does NOT imply a patchable surface; verify real `AdView`/`InterstitialAd`/`BillingClient` usage in app code first. | ChatterBaby has AD_ID but zero in-app ads and no billing (AdMob string was just the ads-identifier SDK) → nothing to patch; commute AD_ID was Firebase-only. |
| Don't fake an "unlock" for server-config-driven lists; confirm a client gate exists. | Vi Movies video quality comes from server `getVideoQualitySettings()` JSON with no client subscription filter; real ceiling is server manifest + Widevine/HDCP — unpatchable. |
| Never store operator-pasted secrets (proxy creds, passwords) in any file, memory, or commit; use them only in-session. | Operator pasted proxy-cheap login + password in chat. |
| An arm64-only "universal" APK fails `INSTALL_FAILED_NO_MATCHING_ABIS` (res=-113 / "no native code compatible with this device's CPU") on 32-bit (armeabi-v7a) TVs; inject the armeabi-v7a native `.so` from the `.apkm` split into the universal (python `zipfile` STORED) so it declares BOTH ABIs, then patch. | `Zee5.apk` shipped only `lib/arm64-v8a/libandroidx.graphics.path.so`; operator's TV was armeabi-v7a → -113 on install. |
| "This app isn't compatible with your TV" = a REQUIRED `<uses-feature>` the device lacks; check `aapt dump badging` — `android.software.live_tv` required=true blocks tuner-less devices (Chromecast/Google TV). Mark it optional (distinct from the touchscreen phone→TV case). | Zee5's TV build declared `live_tv` required=true; Chromecast/Google TV has no tuner → installer rejected it. |
| Verify native-lib page-alignment with the REAL tool (`zipalign -c -p 4` / `-c -v -p 4`), NEVER a hand python data-offset calc — zipalign pads the LOCAL-header extra field, so a central-dir `extra` calc reports false "unaligned". morphe-cli already page-aligns `.so` on rebuild (needed since `extractNativeLibs=false`). | A python offset check flagged both Zee5 `.so` unaligned; `zipalign -c` said `(OK)` for both. |
| Morphe Manager on a phone auto-selects the config split (ABI/density/lang) matching the PATCHING PHONE, so patching a multi-ABI TV `.apkm` on an arm64 phone drops the armeabi-v7a split → the output won't install on a 32-bit TV. Fix: merge the `.apkm` (base + BOTH arch splits) into ONE universal apk with APKEditor (`java -jar ~/tools/APKEditor.jar m -i app.apkm -o app-universal.apk -f`), set `apkFileType = APK`, feed the universal to Morphe. A `.mpp` patch CANNOT override the Manager's split pick. Verify the universal kept `leanback-launchable-activity` + `native-code: 'arm64-v8a' 'armeabi-v7a'`; when several files exist, pick the one whose base has `LEANBACK_LAUNCHER` and whose version matches the Constants target (hotstar: only the `.apkm` base is leanback; the "chromecast"/"streamer" universals are a different version and NOT leanback). | Operator patches TV OTT apps with Morphe on a phone; the phone-arch-only output failed to install on the differently-ABI'd Android TV. |

## Signing (morphe-cli + Morphe.keystore)

| Rule | Why |
|------|-----|
| Morphe.keystore is **BKS**: store password EMPTY, alias **`Morphe`**, key/entry password **`Morphe`**. Signer CN=Morphe, SHA-256 `6f2bc561be3c68479b53b19c10c3b7227dc2f18bca3aa8a105b45a80955af3f7`. | Recovered by listing with the BC provider; matches phone builds. |
| Default sign = `--keystore Morphe.keystore` with NO password/alias flags (CLI defaults alias=Morphe pw=Morphe). Do NOT pass `--keystore-password` (BKS integrity check fails). | learn.md: `--keystore-password Morphe` → "KeyStore integrity check failed". |
| morphe-cli's "Keystore does not contain entry with alias Morphe Key" is a MASK: it tried alias=Morphe/pw=Morphe first, that step threw, and it fell back to legacy alias `Morphe Key` + "" which doesn't exist. The real error is the FIRST attempt (often the APK-signing step), swallowed by `addStepResult`. | Cost ~10 build cycles on AYA Baby AI (82 MB) chasing a phantom alias error. |
| When morphe-cli signing masks the real error, build `--unsigned` then sign with `apksigner` using the key exported to PKCS12 (`keytool -importkeystore -srcstoretype BKS -srcstorepass "" -srckeypass Morphe -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath <morphe-cli.jar>`). | Only reliable path to sign AYA and to see the true signing failure. |
| If apksigner then fails in `getMinSdkVersionFromApk → getAndroidManifestFromApk → LocalFileRecord`, `zipalign -p -f 4` the unsigned APK and pass `apksigner sign --min-sdk-version <N>` (skips the manifest read). Alias in the exported PKCS12 is lowercase `morphe`. | AYA Baby AI's manifest ZIP entry broke apksig's auto minSdk detection; explicit `--min-sdk-version 26` + zipalign signed it CN=Morphe. |
| To read/convert the BKS keystore with keytool, load BouncyCastle from the fat jar: `-storetype BKS -provider org.bouncycastle.jce.provider.BouncyCastleProvider -providerpath ~/tools/morphe-cli/morphe-cli.jar`. Plain keytool says "Unrecognized keystore format". | keytool has no BKS provider by default. |
| morphe-cli CLI options need the `=` form for values with the fallback logic (`--keystore-entry-alias=Morphe`), not space-separated. | Spaced `--keystore-entry-alias "Morphe"` was ignored on AYA; `=Morphe` was honored. |

## Patch build & reverse-engineering

| Rule | Why |
|------|-----|
| `patches-list.json` is GENERATED by the `generatePatchesList` gradle task — never hand-edit; run `./gradlew generatePatchesList` and commit the result. | It feeds README + bundle metadata; hand edits drift from source. |
| Native-game IAP: find the JNI command dispatcher (e.g. `MainActivity.command(String)`); if ownership is answered by a Java getter returning "true"/"false" it's DEX-patchable, else it's in the .so and is not. Always caveat native re-verify. | Does Not Commute premium = `AndroidStore.isProductIdRestored()` via `MainActivity.command`; game logic in libcommute.so is not bytecode-patchable. |
| Force `getUpdateType()`-style boxed getters carefully: returning null NPE-crashes when the caller unboxes; return a safe value (e.g. version getter → "0"). | Vi forced-update: `getUpdateType()` is `.intValue()`'d immediately; patched `getVersionNumber()`→"0" instead. |
| Verify a paste-added `.java` isn't truncated at 80 cols (unterminated `/**`): `awk '{o+=gsub(/\/\*/,"&")-gsub(/\*\//,"&")} END{print o}' file.java` must print 0. | `DeviceIdSpoof.java` was cut at 80 chars; unterminated `/**` ate `gaid()`/`widevineId()` bodies → gradle compile break only surfaced at `:patches:build`. |
| Edit tool: match indentation EXACTLY (class-level javadoc col 0 ` * `, method 4-space); CRLF vs LF is normalized but indent width is not. | The DeviceIdSpoof comment edit failed on 5-space vs 1-space indent. |
| BFF logout/block screens (JioHotstar `DeviceLoggedOutFragment` / `BffUserLoggedOutWidget`, `DeviceRestrictionContainerWidget`) are server protobuf widgets rendered from `/v2/start`; the block text is NOT in the APK and the screen is not client-patchable. | Confirmed across all 28 splits + dex; "Go to home" re-runs the server's own BffActions → same screen loop. |

## Zee5 (com.graymatrix.did)

| Rule | Why |
|------|-----|
| Zee5 video ads are CLIENT-side ad-tag insertion, NOT stream-baked: `PlaybackViewModel.toMediaConfig()` builds `com.zee.mediaplayer.config.AdConfig` from `MediaData.adsUrl`/`adTags`; ad-free users get null `adsUrl`. Block by forcing `MediaData.getAdsUrl()`→null, `getAdTags()`→`Collections.emptyList()`, `FetchPlaybackUrlUseCase.toPreRollDaiSlateMedia()`→null (video) and `HomeActivityViewModel.getNeedToShowNativeMastheadAd()`→false + `FetchPlaybackUrlUseCase.toPauseAds()`→null (display). | Operator insisted "ads not part of the stream, better solution" — confirmed; this reproduces the app's own ad-free path (crash-safe, fewer requests, not laggier). |
| Zee5 has NO geo/VPN/tamper/root block — `isRooted`/`isEmulator`/`attestation` are telemetry ONLY (Mixpanel `SuperProperty`, `DeviceUtils`, `DefaultPlatformHelper`); `getSignature`/SignatureVerif is a payment-model field in `Head.java`. Do NOT patch them (non-gating; patching = risk for zero benefit). | Operator asked to disable them fearing a Hotstar-style block; verified nothing gates login/playback on them. |
| The ONLY hard client block screen is forced-upgrade: `BlockerScreenInfo.isBlocked()`→false (fields `isBlocked`/`isForceUpgrade`/`isUpdateNowVisible` drive only the upgrade wall). Premium = server entitlement + Widevine (unpatchable); server-DAI mid-roll for DAI-only titles is stream-baked. | Confirmed by reading `BlockerScreenInfo` + the ad/entitlement paths; no other client wall exists. |
| Zee5 model/ViewModel/usecase names are NOT obfuscated — fingerprint on `definingClass` + `name` (+ returnType), no opcode patterns needed. Chromecast target = the single universal `Zee5.apk` (v5.83.2, arm64) with the armeabi-v7a split `.so` merged in; launcher activity `com.zee5.android.launch.presentation.AppStartActivity` (LEANBACK). The `.apkm` is an older split (v5.82.7). | All 6 Zee5 fingerprints resolved first try on definingClass+name; the `.apk` (not `.apkm`) is the Chromecast build. |

## ChatGPT (com.openai.chatgpt)

| Rule | Why |
|------|-----|
| Android Auto needs a Car app service the app actually ships (`CarAppService`/`MediaBrowserService` + `com.google.android.gms.car.application` meta); if absent there is nothing to patch — AA never routes voice to a third-party `VoiceInteractionService` and Google whitelists AA apps. | ChatGPT ships a full assistant stack (VoiceInteractionService + ASSIST + AssistantActivity + voice QuickTile) but ZERO Car surface, so "voice on Android Auto" was not patchable. |
| Show an app UI over the lockscreen by adding `android:showWhenLocked="true"` + `android:turnScreenOn="true"` to the specific activity (resourcePatch); flag only the voice/assist activity, never MainActivity, so private content stays off the lock. User must still set the app as default Digital Assistant (OS setting, not patchable). | ChatGPT AssistantActivity + AssistantProxyActivity had no showWhenLocked, so the voice UI could not appear over the keyguard. |
| Renaming an app's package to coexist can BREAK sign-in when login is web-OAuth over verified https App Links (assetlinks.json is package+cert bound) or Google/Apple sign-in; warn the operator, keep it a toggleable patch, and offer label-only (no rename) as the login-preserving fallback. | ChatGPT rename to `com.openai.chatgpt.morphe` risks breaking Google/Apple/OAuth-redirect login; email/password may still work. |

## Git & docs

| Rule | Why |
|------|-----|
| Keep unrelated pending work OUT of a feature commit that touches shared generated files: restore the other subsystem to HEAD, regenerate `patches-list.json`, commit the feature, then re-apply the pending change uncommitted. | The pending JioHotstar device-id revert would otherwise leak into the vimtv/commute commits via `patches-list.json`. |
| Commit messages carry NO attribution trailer (matches repo convention). | Prior commits (6fcfef2 etc.) have none. |
| Append to Talk.md every handoff: header line `Claude Code [YYYY-MM-DD HH:MM:SS AM/PM]`, separators before/after, emojis + tables; never overwrite. | Operator's global mandate for the shared Codex log. |
| Repo layout + file naming follow `claudedocs/REPO_ORGANIZATION.md`: local APKs live ONLY under `workspace/input/<slug>/` (source) and `workspace/output/<slug>/<slug>-morphe.apk` (built); NO per-app dir at repo root; one `/workspace/` gitignore covers all artifacts. Each app has one lowercase slug = its `patches/.../app/chiggi/<slug>/` package. | 2026-08-14 reorg: 13 loose per-app `output/` dirs + a 770MB `APK/` with chaotic names (AYA, Cry, apkmirror hashes, s25ultra dumps) cluttered the root and made builds ad-hoc. |
| Build a patched APK to `workspace/output/<slug>/<slug>-morphe.apk`; read its input from `workspace/input/<slug>/`; keep the dual-ABI merge and any decompile in the session scratchpad, never the repo. | Same 2026-08-14 reorg; zee5 builds were writing to `zee5/output/` at root. |
| TV-only apps append `(Android TV)` to their `Compatibility(name)` (hotstar, jiotv, vimtv, zee5, sonyliv), verified by a `LEANBACK_LAUNCHER` / `android.software.leanback` manifest check; phone apps get no suffix. After changing a name, run `./gradlew generatePatchesList` and cut a release — the Manager/aggregator only update from a published `.mpp`. | Operator: TV builds were indistinguishable from phone apps in the Morphe Manager list. |

## Session hygiene

| Rule | Why |
|------|-----|
| The scratchpad is wiped at each session boundary — re-extract APKs and re-run jadx; don't assume prior decompiles survive. | jadx output + extracted base.apk vanished between sessions mid-JioHotstar audit. |
| Pass jadx an ABSOLUTE input path; a wrong relative path fails silently (0 files). | A jadx run pointed at the scratchpad instead of `APK/…` produced nothing and a wait-loop hung on its own process name. |
| Don't `pkill -f` broad patterns near background tasks; it kills sibling jobs. | A broad `pkill -f jadx` killed the tracked decompile + waiter (exit 144). |

## CI / release (GitHub Actions semantic-release)

| Rule | Why |
|------|-----|
| `gradlew` must be tracked executable (`git update-index --chmod=+x gradlew`); `core.fileMode=false` on the WSL/Windows checkout silently commits it 100644. | CI Build died with exit 126 "Permission denied" running `./gradlew`, so no release was ever cut. |
| `package.json` must list `conventional-changelog-conventionalcommits` (the `.releaserc` `conventionalcommits` preset needs it; release-notes-generator v14 no longer bundles it). | semantic-release `generateNotes` failed `MODULE_NOT_FOUND: conventional-changelog-conventionalcommits`, blocking every release. |
| Morphe Manager serves the `.mpp` from the GitHub RELEASE named in `patches-bundle.json` `download_url` — pushing source does nothing until semantic-release cuts a new versioned release; the Manager dedupes by version so the bump is required. | Operator reported new patches "not showing in Morphe" while the release stayed at v1.15.1. |
| The repo is main-only; keep `.releaserc` branches = `["main"]`, no `dev` prerelease, and no `@cleyrop-org/semantic-release-backmerge` plugin. | Backmerge ran after a successful release and failed exit 128 (`origin/dev` is not a commit — dev never existed), marking every release run red although the release published. |
| JioHotstar device identity has ONE source: `DeviceInfoStore.a()` (R8 `a`, suspend, param `LTj/a;`) reads `Settings.Secure "android_id"` (the only android_id read in the app) and caches it; it feeds the `X-HS-Device-Id` header AND both `/v2/start` body device-id entries (`com.hotstar.bff.utils.c` calls it twice). Spoof there, nowhere else. | ANDROID_ID is signing-key-keyed and survives app-data-clear, so the re-signed build kept resending the same flagged id → "device logged out" on every launch; the Kg/b GAID/Widevine spoof was the WRONG identifier. |
