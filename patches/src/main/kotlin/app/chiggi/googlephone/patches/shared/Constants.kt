package app.chiggi.googlephone.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // Google Phone (com.google.android.dialer) PHONE build — not leanback, so NO "(Android TV)" suffix.
    // Heavily R8-obfuscated: feature logic lives in defpackage.* single-letter classes, but Phenotype
    // flag names and the zqz.i("com/android/dialer/...") log-path literals survive, so patches anchor on
    // string literals (+ returnType + params), never on the unstable obfuscated class names.
    //
    // Reality check (verified in decompile): every Pixel feature here is a patchable client boolean, but
    // the payload behind the flag is NOT in the APK — Call Recording needs CAPTURE_AUDIO_OUTPUT
    // (signature|privileged, /system/priv-app only) and Call Screen needs an on-device SODA speech model.
    // A re-signed sideload therefore shows live UI that records empty audio / dead-ends Call Screen. These
    // patches enable the UI on operator's explicit request; they do not make the features functional on a
    // non-Pixel device. arm64-v8a universal, pinned to 235.0.965622757.
    val COMPATIBILITY_GOOGLE_PHONE = Compatibility(
        name = "Phone by Google",
        packageName = "com.google.android.dialer",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1A73E8, // fallback tint (Google blue); Manager extracts the real icon
        targets = listOf(
            AppTarget(
                version = "235.0.965622757-downloadable",
                minSdk = 30,
            ),
        ),
    )
}
