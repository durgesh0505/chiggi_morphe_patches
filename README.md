# 🧩 Chiggi Morphe Patches

Third-party [Morphe](https://morphe.software) patches for **SonyLIV (Android TV)**,
**Nutrilio (phone)**, **Threads (phone)**, **Arrow Puzzle (game)** and **CrazyGames (game portal)**.

## ❓ About

A set of patches that modify these apps at the bytecode/resource level, applied with
[Morphe](https://github.com/MorpheApp) (a fork of ReVanced). These patches are an independent
project and are **not affiliated with Sony, SonyLIV, Nutrilio, Meta, Threads, Instagram, ReVanced,
or the Morphe project**.

| App | Package | Tested version | Notes |
|-----|---------|----------------|-------|
| SonyLIV | `com.sonyliv` | `6.23.1` (Android TV / leanback) | Media3 (ExoPlayer) |
| Nutrilio | `net.nutrilio` | `1.20.2` (phone) | split APKS bundle |
| Threads | `com.instagram.barcelona` | `434.0.0.41.74` (phone) | Instagram codebase; split APKS bundle |
| Arrow Puzzle | `com.easybrain.arrow.puzzle.game` | `1.7.0` (phone) | Easybrain Unity game; single universal APK |
| CrazyGames | `com.crazygames.crazygamesapp` | `1.6.7` (phone) | Capacitor (Ionic) WebView portal; single universal APK |

## 🩹 SonyLIV patches

| Patch | What it does | Status |
|-------|--------------|--------|
| **Remove video ads** | Forces `PlayerUtil.isAdEnable()` to false so the player neither requests nor plays ads. Removes client-side (Google IMA) ads. | ✅ Applies cleanly; pending on-device confirmation |
| **Auto skip intro and recap** | Automatically skips intro, recap and song segments as soon as the "Skip" button would appear, without waiting for a tap. | ✅ Applies cleanly; pending on-device confirmation |
| **Auto-skip Up Next** | Plays the next episode immediately when the "Up Next" card appears, skipping the auto-play countdown. | ✅ Applies cleanly; pending on-device confirmation |
| **Hide promo banners** | Hides the CleverTap-driven subscribe/premium promo banners and promo trays on the home screen. Content rows and CleverTap pop-ups/overlays are unaffected. | ✅ Applies cleanly; pending on-device confirmation |
| **Suppress geo / VPN block** | Downgrades the geo / "VPN detected" block to a generic error so the dedicated block screens aren't shown. Does **not** grant access — geoblocking is server-side by IP; a valid in-region (India) connection is still required. | ✅ Applies cleanly; pending on-device confirmation |
| **Disable AppsFlyer tracking** | Disables AppsFlyer attribution and event tracking by forcing `isAppsFlyerSupported()` to false. | ✅ Applies cleanly |
| **Disable Firebase tracking** | Disables Firebase Analytics, Crashlytics and Performance collection via manifest flags. Push notifications are unaffected. | ✅ Applies cleanly |
| **Disable forced update** | Removes the forced ("immediate") and optional ("flexi") "update available" popup shown on the home screen. | ✅ Applies cleanly |
| **Change app name** *(opt-in)* | Renames the app shown under the launcher icon. Editable in patch options (**App name**, pre-filled `Sony LIV`). | ✅ Verified |
| **Change package name** *(opt-in)* | Renames the package so the patched app installs alongside the original, rewriting provider authorities. Editable in patch options (**Package name**, pre-filled `com.sonyliv.chiggi`). | ✅ Verified |

All patches are verified to **resolve and apply** against `com.sonyliv` 6.23.1 using `morphe-cli`.
Runtime behaviour should still be confirmed on a device.

### Notes & limitations

- **Ads on live content** — "Remove video ads" disables the client-side (Google IMA) ad path.
  SonyLIV also uses server-side ad insertion (AWS MediaTailor / VisualOn SSAI) for some live
  streams; ads stitched into the video server-side cannot be removed by a client patch and may
  still appear on live content.
- **CleverTap** is intentionally **not** disabled. It is dependency-injected and drives in-app
  overlays / native-display UI, so disabling it would crash parts of the app. "Disable analytics"
  here covers AppsFlyer and the Firebase stack only.

## 🩹 Nutrilio patches

| Patch | What it does | Status |
|-------|--------------|--------|
| **Unlock Plus** | Forces the premium gate (`PremiumModule.t2()`) to true, unlocking all Nutrilio PLUS+ features (custom colors, all charts, app lock, all tracking options). Client-side only; does not grant a real Google Play subscription. | ✅ Applies cleanly; pending on-device confirmation |
| **Disable analytics** | Disables Firebase/Google Analytics, Crashlytics and Performance via manifest flags and removes the advertising-id (`AD_ID`) permissions. Push notifications are unaffected. | ✅ Applies cleanly |
| **Add food search bar** *(opt-in)* | Adds a live search box below each meal-time header in the day/meal form; typing filters that meal's food chips. Filtering only hides chips, so already-added items stay added. | ✅ Applies cleanly; pending on-device confirmation |
| **Local backup & restore** *(on by default)* | Adds a native **Local backup & restore** row to the Backup screen (above *Restore Backup*) that opens Nutrilio's built-in (normally hidden) local file backup/restore. There you can export everything — all entries, images and settings — to a portable `.nutrilio` file (a ZIP) via the share sheet, and restore one from a file you pick. No Google account or premium needed. | ✅ Applies cleanly; pending on-device confirmation |
| **Change app name** *(on by default)* | Renames the app to **Nutrilio Morphe** (editable in patch options). | ✅ Verified |
| **Change package name** *(on by default)* | Renames the package to **net.nutrilio.morphe** so it installs alongside the original (editable in patch options). | ✅ Verified |

### Notes & limitations

- **Version** — pinned to `1.20.2`. A null ("any") version is rejected by Morphe Manager (it makes
  the whole source fail to load), and Nutrilio is R8-obfuscated, so the two bytecode patches
  (**Unlock Plus**, **Add food search bar**) only verifiably resolve on `1.20.2`. They are hardened
  (Unlock Plus anchors on a stable backend string instead of the obfuscated method name; the search
  bar resolves resource ids by name at runtime) to tolerate minor drift, but a different version may
  need re-fingerprinting.
- **Add food search bar** is the most update-fragile patch (it hooks the obfuscated form adapter)
  and is **off by default** so it can never block the other patches.
- **Local backup & restore** reuses Nutrilio's own backup engine, so the file it produces keeps the
  `.nutrilio` extension (it is a ZIP internally) — keep that extension or restore won't recognise it.
  This is a **local file** backup; Google **Drive** auto-backup is separate and needs Google
  Sign-In, which does not work on a re-signed/renamed build (the OAuth client is bound to the
  original signature and package), so Drive backup is effectively unavailable on the patched app.

## 🩹 Threads patches

| Patch | What it does | Status |
|-------|--------------|--------|
| **Hide ads** | Hides sponsored posts and injected "suggested"/netego units from the feed via two hooks: marks every media as organic (`Media.DED()` → false, covers server-inline ads) and blocks the feed ad-injection scheduler (`BarcelonaSpoolFeedCacheHandler` → no inject, covers injected ads). Client-side only. | ✅ Applies cleanly; pending on-device confirmation |
| **Remove AD_ID permission** | Removes the advertising-id (`AD_ID` / `ACCESS_ADSERVICES_AD_ID`) permissions so the device ad id can't be read. Does **not** disable Meta's core analytics. | ✅ Applies cleanly |
| **Change app name** *(on by default)* | Renames the app to **Threads Morphe** (editable in patch options). | ✅ Verified |
| **Change package name** *(on by default)* | Renames the package to **com.instagram.barcelona.morphe** so it installs alongside the original (editable in patch options). | ✅ Verified |

### Notes & limitations

- **Version** — pinned to `434.0.0.41.74`. Threads is built from the heavily R8-obfuscated Instagram
  codebase, whose class/method names shift on nearly every Meta release; the **Hide ads** hooks are
  anchored on stable *named* classes but a different version may still need re-fingerprinting.
- **Account / integrity risk** — Threads is a Meta account app. Re-signing and renaming the package
  can trip Play Integrity / Meta app-attestation, which may cause feed-refresh failures, forced
  logout, or account action. Test on a secondary account first. If login breaks, disable **Change
  package name**.
- **Analytics** — Threads uses Meta's own telemetry (analytics2 / OneFabric), not Firebase, so there
  is no manifest flag to flip; only the `AD_ID` permission is removed.
- **Scope** — **Hide ads** forces the global isAd predicate false, so it also suppresses ad
  treatment on non-feed surfaces (e.g. reels). Server-side fetching of ads is unchanged; the patch
  only refuses to surface them.

## 🩹 Arrow Puzzle patches

| Patch | What it does | Status |
|-------|--------------|--------|
| **Remove ads** | Removes **all** ads (banner + interstitial + rewarded) by neutering the Unity↔Android ad facade (`com.easybrain.ads.unity.AdsPlugin`): availability/show booleans return false, banner shows are no-ops, banner height is 0. One named chokepoint covers every mediation network (ironSource/AppLovin/AdMob/Pangle). | ✅ Applies cleanly; pending on-device confirmation |
| **Disable analytics** | No-ops Easybrain analytics event upload (`AnalyticsPlugin.AnalyticsSendEvent` + the central `AnalyticsController` collector). SDK init is left intact so ad removal stays stable. | ✅ Applies cleanly |
| **Remove AD_ID permission** | Removes the advertising-id + Ad Services (Privacy Sandbox) permissions. | ✅ Applies cleanly |
| **Change app name** *(on by default)* | Renames the app to **Arrow Puzzle Morphe** (editable in patch options). | ✅ Verified |
| **Change package name** *(on by default)* | Renames the package to **com.easybrain.arrow.puzzle.game.morphe** so it installs alongside the original (editable in patch options). | ✅ Verified |

### Notes & limitations

- **Unity game** — gameplay is native (IL2CPP), but ads + analytics run on the Android/DEX side via the Easybrain SDK, which is where these patches apply.
- **Removing all ads removes rewarded ads too** — watch-to-earn rewards (coins/hints) no longer work, by design.
- **Native anti-tamper** — the game ships `libsigner.so` / `libpglarmor.so`; a re-signed + renamed package may trip a native integrity check. If the app crashes or closes on launch, disable **Change package name** (and test the unrenamed build).
- **Version** — pinned to `1.7.0`. The ad/analytics JNI facades (`AdsPlugin`/`AnalyticsPlugin`) keep stable names, but the central analytics collector is obfuscated and may need re-fingerprinting on updates.

## 🩹 CrazyGames patches

| Patch | What it does | Status |
|-------|--------------|--------|
| **Bypass Play license check** *(on by default)* | Neuters Google Play's Automatic Integrity Protection (PairIP) license check (`com.pairip.licensecheck.LicenseClient`) that, on a re-signed/sideloaded build, redirects the app to the Play Store (*"get it on Google Play"*). **Required for the patched app to open.** | ✅ Applies cleanly; pending on-device confirmation |
| **Remove ads** | Neuters the native Capacitor AdMob plugin (`com.getcapacitor.community.admob.AdMob`): no video plays for interstitial or rewarded ads — the show methods instead fire the ad's lifecycle events (`Showed`/`Dismissed`, or `Reward`/`Dismissed`) so the game's loading overlay dismisses and **rewarded ads still credit their reward**; banners never display. One named chokepoint covers AdMob and its Pangle / Audience Network mediation. | ✅ Applies cleanly; pending on-device confirmation |
| **Disable analytics** | Stops native Sentry crash/telemetry upload by neutering the Capacitor Sentry plugin's native init (`SentryCapacitor.initNativeSdk`). Firebase auth, ConfigCat and push are unaffected. | ✅ Applies cleanly |
| **Remove AD_ID permission** | Removes the advertising-id + Ad Services (Privacy Sandbox) permissions (`AD_ID`, `ACCESS_ADSERVICES_*`). | ✅ Applies cleanly |
| **Change app name** *(on by default)* | Renames the app to **CrazyGames Morphe** (editable in patch options). | ✅ Verified |

### Notes & limitations

- **Play Automatic Integrity Protection (PairIP)** — CrazyGames ships with Google Play's PairIP licensing guard, which redirects any non-Play / re-signed install to the store. **Bypass Play license check** is required and on by default; without it the patched app won't open. This build has no PairIP code-virtualization (VMRunner), so neutering the license check is safe.
- **Capacitor WebView app** — most of CrazyGames is the remote Next.js portal (`app.crazygames.com`) rendered in a WebView. These patches only touch the **native** ad/telemetry shell.
- **In-page web video ads remain** — the pre/mid-roll ads shown *inside* a game come from CrazyGames' servers as web content, not the native ad SDK, so they are not in the app bytecode and **cannot be removed here**. "Remove ads" kills the native interstitial/banner/rewarded layer only.
- **Auto-reward is client-side** — the rewarded bypass fires the reward listener event the web grants on. If CrazyGames adds server-side reward verification, the reward simply won't be granted (no crash).
- **Package kept** — the package is **not** renamed, so Google/Facebook/Apple sign-in keeps working. The patched app installs over / conflicts with the Play Store version rather than alongside it.
- **Native anti-tamper** — the app ships ByteDance Pangle's `libpglarmor.so` / `libsigner.so`; a re-signed APK may trip a native integrity check (best case Pangle silently no-fills, worst case a launch crash — untested).
- **Version** — pinned to `1.6.7`. The AdMob/Sentry plugin classes and method names are stable, but the Capacitor helper classes (`PluginCall`/`JSObject`/`resolve`) are R8-obfuscated, so the injected bytecode is version-specific.

## 📲 How to use

These patches are distributed as a `.mpp` bundle for Morphe Manager.

- In Morphe Manager → Patch bundles → **Add → Enter URL**, paste the raw
  `patches-bundle.json` URL (this is the source URL the Manager expects — **not** the
  repository URL):
  `https://raw.githubusercontent.com/durgesh0505/chiggi_morphe_patches/refs/heads/main/patches-bundle.json`
- Or download the bundle directly:
  [`patches-1.14.3.mpp`](https://github.com/durgesh0505/chiggi_morphe_patches/releases/latest)

Patch the SonyLIV Android TV APK or the Nutrilio bundle with Morphe, then sideload the result onto
your device (both ship as split APKs; Morphe handles merging and signing).

### Re-patching an APKM locally (helper script)

`scripts/repatch_sonyliv.sh` patches a SonyLIV Android TV `.apkm` with this patch set
(merging splits into one universal APK), applies the app-name / package-name options, and
signs it with a keystore:

```bash
./scripts/repatch_sonyliv.sh path/to/sonyliv.apkm
# or run with no argument to pick the .apkm interactively
```

Override the defaults with environment variables, e.g.
`APP_NAME="My LIV" PACKAGE_NAME=com.sonyliv.custom KEYSTORE=./my.keystore ./scripts/repatch_sonyliv.sh app.apkm`.

`scripts/repatch_nutrilio.sh` does the same for the Nutrilio split bundle (accepts the `.zip`
download, copies it to `.apks`, enables the rename + food-search-bar patches, and signs):

```bash
./scripts/repatch_nutrilio.sh net.nutrilio.zip
# defaults: APP_NAME="Nutrilio Morphe" PACKAGE_NAME=net.nutrilio.morphe SEARCH_BAR=1
```

Both scripts use a locally built bundle if present, otherwise download the latest release.
Requires JDK 17+, the Android SDK, and [morphe-cli](https://github.com/MorpheApp/morphe-cli).

## 🛠️ Building from source

Requirements: JDK 17+ and the Android SDK (compileSdk 36).

A GitHub Personal Access Token with the `read:packages` scope is required to resolve
`morphe-patcher` from the Morphe GitHub Packages registry. Add it to `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

Then build the patch bundle:

```bash
./gradlew buildAndroid
# Output: patches/build/libs/patches-<version>.mpp
```

List or apply the patches with [morphe-cli](https://github.com/MorpheApp/morphe-cli):

```bash
java -jar morphe-cli.jar list-patches --patches=patches/build/libs/patches-1.14.3.mpp -v
java -jar morphe-cli.jar patch -p patches/build/libs/patches-1.14.3.mpp -o out.apk base.apk
```

## 📜 License

Licensed under the [GNU General Public License v3.0](LICENSE), with the additional GPL Section 7
terms described in [NOTICE](NOTICE). These patches are based on the prior work of
[Morphe](https://github.com/MorpheApp) and [ReVanced](https://github.com/ReVanced); the `NOTICE`
file is preserved as required. This project uses its own identity and is not associated with the
Morphe project name.
