package app.chiggi.bighunter.patches.ads

import app.morphe.patcher.Fingerprint

// Unobfuscated JNI-bridge ad classes. Each is a native->Java entry surface; reward is granted by the
// native call rewardCallback(), the native "processing" lock is released by stopProcess(). Fingerprint
// on definingClass + name (all show/showWithReward/getIsReadyReward share names across the 3 classes).

// ---- AppLovin ----
internal object AppLovinShowFingerprint : Fingerprint(
    definingClass = "LkakarodJavaLibs/data/KKJAdsAppLovin;",
    name = "show",
    returnType = "V",
    parameters = listOf(),
)

internal object AppLovinShowRewardFingerprint : Fingerprint(
    definingClass = "LkakarodJavaLibs/data/KKJAdsAppLovin;",
    name = "showWithReward",
    returnType = "V",
    parameters = listOf(),
)

internal object AppLovinIsReadyRewardFingerprint : Fingerprint(
    definingClass = "LkakarodJavaLibs/data/KKJAdsAppLovin;",
    name = "getIsReadyReward",
    returnType = "Z",
    parameters = listOf(),
)

// ---- AdMob ----
internal object AdmobShowFingerprint : Fingerprint(
    definingClass = "LkakarodJavaLibs/data/KKJAdsAdmob;",
    name = "show",
    returnType = "V",
    parameters = listOf(),
)

internal object AdmobShowRewardFingerprint : Fingerprint(
    definingClass = "LkakarodJavaLibs/data/KKJAdsAdmob;",
    name = "showWithReward",
    returnType = "V",
    parameters = listOf(),
)

internal object AdmobIsReadyRewardFingerprint : Fingerprint(
    definingClass = "LkakarodJavaLibs/data/KKJAdsAdmob;",
    name = "getIsReadyReward",
    returnType = "Z",
    parameters = listOf(),
)

// ---- Unity (reward NOT wired to a Java rewardCallback; only suppress the ad) ----
internal object UnityShowFingerprint : Fingerprint(
    definingClass = "LkakarodJavaLibs/data/KKJAdsUnity;",
    name = "show",
    returnType = "V",
    parameters = listOf(),
)

internal object UnityShowRewardFingerprint : Fingerprint(
    definingClass = "LkakarodJavaLibs/data/KKJAdsUnity;",
    name = "showWithReward",
    returnType = "V",
    parameters = listOf(),
)

internal object UnityIsReadyRewardFingerprint : Fingerprint(
    definingClass = "LkakarodJavaLibs/data/KKJAdsUnity;",
    name = "getIsReadyReward",
    returnType = "Z",
    parameters = listOf(),
)
