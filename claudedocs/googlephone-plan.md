# Google Phone (com.google.android.dialer) — patch plan

Slug: `googlephone`. Input: `workspace/input/googlephone/googlephone.apk` (v235.0.965622757, arm64-v8a, phone app — NO leanback, so no "(Android TV)" suffix). Output: `workspace/output/googlephone/googlephone-morphe.apk`.

## Goal (operator-approved 2026-09-08)
Enable the Pixel-locked **Call Recording** and **Call Screen ("call assist")** UI via flag-flip, KNOWING they do not functionally work on a non-Pixel Samsung S25 Ultra. Operator explicitly chose "Build flag-flip anyway" after being shown the blockers.

## Brutal-honest status (verified in jadx decompile)
- **No screen recording exists** in Google Phone (zero `MediaProjection`). "Screen recording" = Call **audio** Recording.
- Every Pixel feature gates on a trivially-patchable client boolean (Phenotype flag), but the payload behind the flag is NOT in the APK:
  - Call Recording → `AudioRecord(VOICE_CALL/UPLINK/DOWNLINK)` + AudioPolicy → needs `CAPTURE_AUDIO_OUTPUT` = signature|privileged (only `/system/priv-app`). Sideloaded re-signed APK = normal app → permission denied → **records silence/empty**.
  - Call Screen / Hold for Me / Direct My Call → on-device SODA speech model (Speech Services by Google / Google app + offline language pack), absent unless provisioned → **dead UI**.
- No server Pixel-entitlement check; no `Build.MODEL`/`"Pixel"` string check. Device gating = a `Build.DEVICE` allowlist (empty default) + the on-device resources above.

## The two patched gates (both `public final boolean a()`, no params, returnType Z)
| Patch | Obf method (this build) | Real class/method | Force |
|-------|------------------------|-------------------|-------|
| Enable call recording | `defpackage.iwd.a()` | `com.android.dialer.callrecording.impl.canrecord.CanRecord.canRecordCall` | `return true` |
| Enable call screen | `defpackage.jzl.a()` | `com.android.dialer.dobby.enabledfn.DobbyEnabledFn.isEnabled` | `return true` |

Obfuscated class names (`iwd`,`jzl`) shift across builds → fingerprint on stable **string literals** (the `zqz.i("com/android/dialer/...")` log-path + a decision string) + `returnType=Z` + `parameters=listOf()`, no `definingClass`. Same idiom as hotstar ad fingerprints.

## Risk / gotchas
- Forcing `CanRecord.canRecordCall`→true lets the recording path start `AudioRecord(4,...)`; on a non-privileged app this throws SecurityException / returns uninitialized → the tap-to-record may crash or produce empty files. Accepted by operator.
- Re-signing Google Dialer + signing into a real Google account = ban vector → throwaway account only for testing (RULEBOOK Universal rule).
- Samsung One UI's own dialer already records calls natively in most regions — that is the working path, not this.

## Build
1. `./gradlew :patches:build` → `patches/build/libs/patches-<v>.mpp`
2. `./gradlew generatePatchesList`
3. `morphe-cli patch --keystore Morphe.keystore ... -i workspace/input/googlephone/googlephone.apk -o workspace/output/googlephone/googlephone-morphe.apk` (default alias/pw, no password flag).
4. Verify both fingerprints resolved (no "could not find" in patch log).
