package app.chiggi.hotstar.patches.ad

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Ad hook adapted for JioHotstar 26.06.22.3. Approach credited to Paresh-Maheshwari
 * (https://gitlab.com/Paresh-Maheshwari/paresh-patches, GPL-3.0).
 *
 * AdMetadata constructor (Lbd/a; on 26.06.22.3) — controls pre-roll/mid-roll flags + the ssaiTag.
 * Signature identical to the reference: (Z Z Z Z Z String List). Clearing ssaiTag makes the client
 * request a clean (non ad-stitched SSAI) stream, which removes mid-roll/live match ads at the source
 * without fighting the app's ad machinery. Anchored on stable string constants; verified present on
 * 26.06.22.3.
 */
internal object AdMetadataConstructorFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf("Z", "Z", "Z", "Z", "Z", "Ljava/lang/String;", "Ljava/util/List;"),
    strings = listOf("ssaiTag", "perPodPositions"),
)

// Home-feed display/banner (masthead) ad composable. Neutering its render hides the banner; the
// widget container is still laid out, so a small empty gap can remain where the ad was (a proper
// gap-free removal would require filtering the widget out of the server-driven feed model).
internal object DisplayAdComposableFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    strings = listOf("DisplayAd"),
)
