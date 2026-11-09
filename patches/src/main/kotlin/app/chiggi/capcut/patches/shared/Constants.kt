package app.chiggi.capcut.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // CapCut (com.lemon.lvoverseas) phone build, single merged APK (arm64-v8a, 35 dex, no PairIP),
    // pinned to 20.0.0 (versionCode 20001600). Several patches are ported from kveld9's
    // kveld-extra-morphe-patches (GPLv3, written for 19.9.0) and were re-verified against 20.0.0:
    // R8 short names shifted (e.g. CutSamePurchaseViewModel +3 letters) and a protector
    // (com.illusion.light.EffectUtil / libluster.so) turns ~42 classes' methods into JNI natives,
    // which cannot be bytecode-patched; every hook here targets a non-native Java method.
    // See claudedocs/CAPCUT_PATCH_PLAN.md for the full hook map and evidence.
    const val CAPCUT_PACKAGE_NAME = "com.lemon.lvoverseas"

    val COMPATIBILITY_CAPCUT = Compatibility(
        name = "CapCut",
        packageName = CAPCUT_PACKAGE_NAME,
        apkFileType = ApkFileType.APK,
        appIconColor = 0x000000, // fallback tint; Manager extracts the real icon
        targets = listOf(
            AppTarget(
                version = "20.0.0",
                minSdk = 23,
            ),
        ),
    )
}
