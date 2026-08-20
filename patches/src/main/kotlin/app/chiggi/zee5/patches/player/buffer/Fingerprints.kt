package app.chiggi.zee5.patches.player.buffer

import app.morphe.patcher.Fingerprint

/**
 * DefaultPlayerConfig.defaultBuffersForPlaybackMs() supplies ExoPlayer's bufferForPlayback threshold
 * (via MediaUtilsKt.loadControl -> DefaultLoadControl.setBufferDurationsMs(min, max, forPlayback,
 * afterRebuffer)). It normally returns remoteConfig "player_playback_start_buffer_ms" (~2500ms
 * fallback). Lowering it makes playback start after less initial buffering. Must stay <= min buffer
 * (remoteConfig, ~15-50s), so 1500 is safe. Names are not obfuscated.
 */
internal object DefaultBuffersForPlaybackMsFingerprint : Fingerprint(
    definingClass = "Lcom/zee5/androidtv/playback/DefaultPlayerConfig;",
    name = "defaultBuffersForPlaybackMs",
    returnType = "I",
)
