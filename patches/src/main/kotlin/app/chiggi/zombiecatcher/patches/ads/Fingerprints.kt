package app.chiggi.zombiecatcher.patches.ads

import app.morphe.patcher.Fingerprint

private const val ZC_ACTIVITY = "Lfi/twomenandadog/zombiecatchers/ZCActivity;"

internal object AppLovinInitFingerprint : Fingerprint(
    definingClass = ZC_ACTIVITY,
    name = "AppLovinInit",
    returnType = "V",
    parameters = listOf(),
)

internal object AppLovinInitializedFingerprint : Fingerprint(
    definingClass = ZC_ACTIVITY,
    name = "AppLovinisInitialised",
    returnType = "Z",
    parameters = listOf(),
)

internal object HasVideosAvailableFingerprint : Fingerprint(
    definingClass = ZC_ACTIVITY,
    name = "hasVideosAvailable",
    returnType = "Z",
    parameters = listOf(),
)

internal object LoadBannerFingerprint : Fingerprint(
    definingClass = ZC_ACTIVITY,
    name = "loadAppLovinBanner",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
)

internal object ShowInterstitialFingerprint : Fingerprint(
    definingClass = ZC_ACTIVITY,
    name = "showAppLovinInterstitial",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
)

internal object DestroyBannerFingerprint : Fingerprint(
    definingClass = ZC_ACTIVITY,
    name = "destroyBanner",
    returnType = "V",
    parameters = listOf(),
)

internal object ShowBannerFingerprint : Fingerprint(
    definingClass = ZC_ACTIVITY,
    name = "showBanner",
    returnType = "V",
    parameters = listOf(),
)

internal object HideBannerFingerprint : Fingerprint(
    definingClass = ZC_ACTIVITY,
    name = "hideBanner",
    returnType = "V",
    parameters = listOf(),
)

internal object PlayRewardedAdFingerprint : Fingerprint(
    definingClass = ZC_ACTIVITY,
    name = "AppLovinPlayAd",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
)
