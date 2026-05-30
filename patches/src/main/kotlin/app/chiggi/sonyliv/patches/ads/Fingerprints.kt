package app.chiggi.sonyliv.patches.ads

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

private const val PLAYER_UTIL_CLASS = "Lcom/sonyliv/mediaplayer/util/PlayerUtil;"

/**
 * com.sonyliv.mediaplayer.util.PlayerUtil#isAdEnable()
 *
 * Global ad gate. Returns true unless GDPR country or AdsConfig.isAllAdsDisabled.
 * Checked by SLPlayerView / SLPlayerViewModel / HomeActivity before requesting ads.
 */
internal object IsAdEnableFingerprint : Fingerprint(
    name = "isAdEnable",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = listOf(),
    definingClass = PLAYER_UTIL_CLASS,
)

/**
 * com.sonyliv.mediaplayer.util.PlayerUtil#isAdEnable(AssetMetadata)
 *
 * Per-content ad gate (PlayerConfigRepositoryImpl.isAdEnabled delegates here).
 */
internal object IsAdEnableForAssetFingerprint : Fingerprint(
    name = "isAdEnable",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = listOf("Lcom/sonyliv/network/model/core/AssetMetadata;"),
    definingClass = PLAYER_UTIL_CLASS,
)
