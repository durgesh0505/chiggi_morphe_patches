package app.chiggi.sonyliv.patches.player.upnext

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.sonyliv.mediaplayer.player.playerrevamp.SLNextEpisodeUI
 *
 * #updateNextEpisodeAndWatchCreditsUIVisibility(boolean shouldBeVisible)
 * Shows/hides the "Up Next" card. When shouldBeVisible == true the next-episode card appears
 * (and, with auto-binge, counts down before playing the next episode). The card's
 * `nextEpisodeBtn` is wired to `clickListener`, which plays the next content on click.
 */
internal object UpdateNextEpisodeVisibilityFingerprint : Fingerprint(
    name = "updateNextEpisodeAndWatchCreditsUIVisibility",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Z"),
    definingClass = "Lcom/sonyliv/mediaplayer/player/playerrevamp/SLNextEpisodeUI;",
)
