package app.chiggi.ayababy.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    // AYA Baby AI (package com.cry.analyzer, label "AYA Baby AI"). Standard Android (Kotlin) app,
    // no Flutter/Unity/RN. Premium is an auto-renewing subscription managed by the Apphud SDK
    // (com.apphud.sdk) — the app gates premium features (and its ads-or-premium flow) on
    // Apphud.hasPremiumAccess()/hasActiveSubscription(). Ads are Appodeal mediation (AdMob/AppLovin/
    // IronSource/Chartboost); in the ads-or-premium model, unlocking premium also stops them. The
    // Apphud class/method names are not obfuscated, so the entitlement patch anchors on them. Single
    // universal APK, pinned to 6.8.
    val COMPATIBILITY_AYABABY = Compatibility(
        name = "AYA Baby AI",
        packageName = "com.cry.analyzer",
        // Supplied as a single universal APK (not a split bundle).
        apkFileType = ApkFileType.APK,
        appIconColor = 0xF5A8C0, // fallback tint (baby-pink); Manager extracts the real icon
        targets = listOf(
            AppTarget(
                version = "6.8",
                minSdk = 26,
            ),
        ),
    )
}
