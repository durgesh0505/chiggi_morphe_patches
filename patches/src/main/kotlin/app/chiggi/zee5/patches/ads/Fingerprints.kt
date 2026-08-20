package app.chiggi.zee5.patches.ads

import app.morphe.patcher.Fingerprint

/**
 * ZEE5 video ads are client-side ad-tag insertion, NOT baked into the stream: PlaybackViewModel
 * .toMediaConfig() builds the ZMediaPlayer MediaConfig with an AdConfig from MediaData's ad-tag URL
 * (adsUrl) + adTags list. Ad-free/premium users get a null adsUrl and an empty adTags from the server
 * — a fully supported, crash-safe path — so forcing those makes every playback take the ad-free path
 * (no pre/mid-roll ads) with no stream dependency. The pre-roll DAI slate is a separate media prepended
 * from FetchPlaybackUrlUseCase.toPreRollDaiSlateMedia(); returning null there drops the video-start ad.
 * Model/method names are not obfuscated, so these anchor on definingClass + name.
 */
internal object GetAdsUrlFingerprint : Fingerprint(
    definingClass = "Lcom/zee5/android/ui/player/domain/models/MediaData;",
    name = "getAdsUrl",
    returnType = "Ljava/lang/String;",
)

internal object GetAdTagsFingerprint : Fingerprint(
    definingClass = "Lcom/zee5/android/ui/player/domain/models/MediaData;",
    name = "getAdTags",
    returnType = "Ljava/util/List;",
)

internal object ToPreRollDaiSlateFingerprint : Fingerprint(
    definingClass = "Lcom/zee5/android/ui/player/domain/usecases/FetchPlaybackUrlUseCase;",
    name = "toPreRollDaiSlateMedia",
    returnType = "Lcom/zee5/android/ui/player/domain/models/PreRollDaiSlateMedia;",
)

/**
 * Display ads (not video): HomeActivityViewModel.getNeedToShowNativeMastheadAd() gates the home-screen
 * native masthead banner; forcing it false stops the banner from loading. FetchPlaybackUrlUseCase
 * .toPauseAds(PauseDisplayAd) builds the on-pause display ad; returning null removes the pause overlay.
 */
internal object NeedToShowMastheadAdFingerprint : Fingerprint(
    definingClass = "Lcom/zee5/android/home/presentation/HomeActivityViewModel;",
    name = "getNeedToShowNativeMastheadAd",
    returnType = "Z",
)

internal object ToPauseAdsFingerprint : Fingerprint(
    definingClass = "Lcom/zee5/android/ui/player/domain/usecases/FetchPlaybackUrlUseCase;",
    name = "toPauseAds",
    returnType = "Lcom/zee5/android/ui/player/domain/models/PauseDisplayAds;",
)
