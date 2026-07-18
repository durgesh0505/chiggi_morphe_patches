package app.chiggi.jiotv.patches.ad

import app.chiggi.jiotv.patches.shared.Constants.COMPATIBILITY_JIOTV
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Removes JioTV+ ads. Display/banner ads are neutered at the JioAds SDK entry " +
        "(JioAdView.loadAd / cacheAd), and player video ads (pre-roll, mid-roll, post-roll and SSAI " +
        "stitched ads) are removed by no-oping VMAPAdsHelper.fetchAds — the single method that " +
        "fetches and sets up all player ads from the playback-rights model.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_JIOTV)

    execute {
        // JioAds SDK display/banner ads -> never load.
        JioAdViewLoadAdFingerprint.method.addInstructions(0, "return-void")
        JioAdViewCacheAdFingerprint.method.addInstructions(0, "return-void")

        // Player video ads (pre/mid/post-roll + SSAI stitch) -> never fetched.
        VmapFetchAdsFingerprint.method.addInstructions(0, "return-void")
    }
}
