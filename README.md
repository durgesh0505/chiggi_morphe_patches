# 🧩 Chiggi Morphe Patches

Third-party [Morphe](https://morphe.software) patches for **SonyLIV (Android TV)** and
**Nutrilio (phone)**.

## ❓ About

A set of patches that modify these apps at the bytecode/resource level, applied with
[Morphe](https://github.com/MorpheApp) (a fork of ReVanced). These patches are an independent
project and are **not affiliated with Sony, SonyLIV, Nutrilio, ReVanced, or the Morphe project**.

| App | Package | Tested version | Notes |
|-----|---------|----------------|-------|
| SonyLIV | `com.sonyliv` | `6.23.1` (Android TV / leanback) | Media3 (ExoPlayer) |
| Nutrilio | `net.nutrilio` | `1.20.2` (phone) | split APKS bundle |

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
| **Change app name** *(opt-in)* | Renames the app shown under the launcher icon. Editable in patch options (**App name**, pre-filled `Nutrilio Morphe`). | ✅ Verified |
| **Change package name** *(opt-in)* | Renames the package so the patched app installs alongside the original, rewriting provider authorities. Editable in patch options (**Package name**, pre-filled `net.nutrilio.morphe`). | ✅ Verified |

### Notes & limitations

- **Any-version compatibility** — the Nutrilio patches declare no fixed version, so the Manager
  offers them on any version. Nutrilio is R8-obfuscated, so the two bytecode patches (**Unlock
  Plus**, **Add food search bar**) match obfuscated symbols. They are hardened — Unlock Plus
  anchors on a stable backend string instead of the obfuscated method name, and the search bar
  resolves resource ids by name at runtime — but only `1.20.2` is verified. The resource/manifest
  patches (**Disable analytics**, **Change app name/package**) are version-agnostic.
- **Add food search bar** is the most update-fragile patch (it hooks the obfuscated form adapter)
  and is **off by default** so it can never block the other patches.

## 📲 How to use

These patches are distributed as a `.mpp` bundle for Morphe Manager.

- Add as a custom source in Morphe Manager using this repository URL:
  `https://github.com/durgesh0505/chiggi_morphe_patches`
- Or download the bundle directly:
  [`patches-1.9.0.mpp`](https://github.com/durgesh0505/chiggi_morphe_patches/releases/latest)

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
java -jar morphe-cli.jar list-patches --patches=patches/build/libs/patches-1.9.0.mpp -v
java -jar morphe-cli.jar patch -p patches/build/libs/patches-1.9.0.mpp -o out.apk base.apk
```

## 📜 License

Licensed under the [GNU General Public License v3.0](LICENSE), with the additional GPL Section 7
terms described in [NOTICE](NOTICE). These patches are based on the prior work of
[Morphe](https://github.com/MorpheApp) and [ReVanced](https://github.com/ReVanced); the `NOTICE`
file is preserved as required. This project uses its own identity and is not associated with the
Morphe project name.
