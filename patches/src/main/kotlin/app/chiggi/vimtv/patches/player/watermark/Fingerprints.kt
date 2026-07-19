package app.chiggi.vimtv.patches.player.watermark

import app.morphe.patcher.Fingerprint

/**
 * com.yupptv.ottsdk.model.user.Configs#getWaterMarkInfoOnPlayer()
 *
 * The per-content watermark shown over the player. ExoPlayerFragment renders the ImageView
 * R.id.watermark_IV only when this getter returns a non-empty JSON string (which carries the
 * logo/position for the item being played — hence the watermark changes with the content); when it
 * is null/empty the fragment's own guard calls setVisibility(GONE). This getter is the sole consumer
 * (getShowWatermark() is dead code), so forcing it to null hides the watermark with no side effects.
 * Real class/method names are kept (light obfuscation), so this anchors on definingClass + name.
 */
internal object GetWaterMarkInfoOnPlayerFingerprint : Fingerprint(
    definingClass = "Lcom/yupptv/ottsdk/model/user/Configs;",
    name = "getWaterMarkInfoOnPlayer",
    returnType = "Ljava/lang/String;",
)
