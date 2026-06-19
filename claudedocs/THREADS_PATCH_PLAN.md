# Threads (Barcelona) Patch Plan

**Target:** `com.instagram.barcelona` v434.0.0.41.74 (versionCode 510406907)
**APK:** `APK/com.instagram.barcelona_434.0.0.41.74-510406907.apkm` (split: base.apk 56 MB + arm64 native 72 MB + dpi splits; arm64-only; minSdk 28; targetSdk 36)
**Source namespace:** `app.chiggi.threads` (GPL §7c — output branding may be "morphe", project not named "Morphe")
**Pinned version:** 434.0.0.41.74 (any-version not feasible — obfuscated names shift per build)

## Approved scope (user, 2026-06-18)
1. **Hide feed ads / sponsored** (netego filter) — PRIMARY request
2. **Remove AD_ID** permission
3. **Branding** — app name + package → morphe variant (side-by-side install)

User accepted: account-ban/integrity risk; pin-to-version maintenance.

## Key facts established
| Fact | Value | Consequence |
|---|---|---|
| Codebase | Instagram engine ("Barcelona") | IG patch precedents apply |
| Obfuscation | package paths kept, class/method names scrambled | fingerprint by anchor string |
| Localization | Meta runtime i18n; `aapt2 dump strings` empty | no resource strings; bytecode only |
| Feed type enum | `X/0sO` (values incl. "sponsored","netego") | classifier anchor for ad-hide |
| Launcher activity | `com.instagram.barcelona.mainactivity.BarcelonaActivity` | for branding label |
| AD_ID | `com.google.android.gms.permission.AD_ID` present | removable via manifest |
| Billing | `com.android.vending.BILLING` | no premium tier → no unlock patch |

## Patch designs

### P1 — Hide ads / sponsored (bytecodePatch)
- Hook: TBD from smali RE (candidate: method classifying feed item via `X/0sO` enum, or feed-list builder).
- Mechanism: force is-ad path so sponsored/netego items are dropped/hidden without NPE downstream.
- Anchor on stable string ("netego"/"sponsored") via `custom = { method, classDef -> ... }`.
- `default = true`. compatibleWith(COMPATIBILITY_THREADS).
- Verification limit: cannot device-test; morphe-cli apply + smali presence check only. Needs user device confirmation.

### P2 — Remove AD_ID (resourcePatch)
- Strip `uses-permission` `com.google.android.gms.permission.AD_ID` + `android.permission.ACCESS_ADSERVICES_AD_ID`.
- No Firebase flag-flip (Meta uses own analytics2/OneFabric, not Firebase Analytics gates).
- `default = true`.

### P3a — Change app name (resourcePatch)
- Option `appName` default "Threads Morphe". Override `<application android:label>` + launcher activity label.
- `default = true`.

### P3b — Change package name (resourcePatch, finalize)
- Option `packageName` default `com.instagram.barcelona.morphe`. Rewrite provider authorities + own permissions.
- ⚠️ Higher breakage risk on Meta apps (FB SSO, content providers). `default = true` but documented; user can toggle off.

## Files to create
```
patches/src/main/kotlin/app/chiggi/threads/patches/
  shared/Constants.kt                         # COMPATIBILITY_THREADS
  ads/Fingerprints.kt                          # ad-hide fingerprint(s)
  ads/HideAdsPatch.kt                          # P1
  misc/analytics/RemoveAdIdPatch.kt            # P2
  misc/branding/ChangeAppNamePatch.kt          # P3a
  misc/packagename/ChangePackageNamePatch.kt   # P3b
```

## Build / verify pipeline (same as Nutrilio)
1. `./gradlew :extensions:extension:build buildAndroid generatePatchesList` (delete old `build/libs/*.mpp` first)
2. morphe-cli apply to `.apks` → inspect manifest (aapt2) + classes.dex (ad hook strings present)
3. Bump `gradle.properties` version, update `patches-bundle.json` + `patches-list.json`, README
4. Commit; **do not push/release until user approves**

## Status (2026-06-18)
- ✅ Built `patches-1.10.0.mpp`; all 4 patches `Applied` via morphe-cli (Hide ads fingerprints resolved).
- ✅ Verified: package `com.instagram.barcelona.morphe`, label `Threads Morphe`, AD_ID removed; baksmali confirms `Media.DED()Z` + `A0g()Z` both `return false`.
- ✅ Patched APK at `threads/output/threads_morphe_patched.apk` (gitignored).
- ⏳ NOT pushed/released; NOT device-tested. Awaiting user feed/login confirmation before publishing v1.10.0.

## Open risks
- Ad-hide hook unverifiable without device → may need iteration with user.
- Package rename may break Threads login/providers → keep as toggle.
- Re-sign may trip Meta integrity → user accepted.
