package app.chiggi.jiotv.patches.ad

import app.morphe.patcher.Fingerprint

/**
 * JioTV+ ad chokepoints (real class names kept, light obfuscation):
 *  - JioAds SDK display/banner ads: JioAdView.loadAd() / cacheAd()
 *  - Player video ads (pre/mid/post-roll + SSAI stitch): VMAPAdsHelper.fetchAds(...) — the single
 *    entry that fetches and sets up all VMAP/IMA player ads from the playback-rights model.
 * Neutering these to return-void removes ads at the source without touching the player itself.
 */
internal object JioAdViewLoadAdFingerprint : Fingerprint(
    definingClass = "Lcom/jio/jioads/adinterfaces/JioAdView;",
    name = "loadAd",
    returnType = "V",
    parameters = emptyList(),
)

internal object JioAdViewCacheAdFingerprint : Fingerprint(
    definingClass = "Lcom/jio/jioads/adinterfaces/JioAdView;",
    name = "cacheAd",
    returnType = "V",
    parameters = emptyList(),
)

internal object VmapFetchAdsFingerprint : Fingerprint(
    definingClass =
        "Lcom/jio/media/stb/ondemand/patchwall/player/playerutils/VMAPAdsHelper;",
    name = "fetchAds",
    returnType = "V",
)
