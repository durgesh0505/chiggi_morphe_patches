package app.chiggi.hotstar.patches.playback

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Playback capability fingerprints. Approach credited to Paresh-Maheshwari (GPL-3.0). Anchored on
 * codec/HDR blacklist strings; verified present on 26.06.22.3.
 */

// 4K resolution support check — anchors on the "4k" blacklist key.
internal object Is4kSupportedFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("4k"),
)

// Video codec support check (H265/VP9/AV1).
internal object IsCodecSupportedFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    strings = listOf("supportsCodec Picked from cache"),
)

// Dolby Vision / HDR support check — "dv" + "bklst" (blacklist) identify the MediaCodecUtils method.
internal object IsDolbyVisionSupportedFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;", "Z", "I", "Z"),
    strings = listOf("dv", "bklst"),
)
