package app.chiggi.zee5.patches.player.autoskip

import app.morphe.patcher.Fingerprint

/**
 * PlaybackControlsFragment.onMarkerHit(PlayerTimelineActions) fires when the playhead enters a
 * timeline marker window (intro / recap / next-episode / ad). It shows the "Skip Intro"/"Skip Recap"
 * button; tapping the button runs getMediaController().getTransportControls().seekTo(marker
 * .getEndTimeMillis()). Marker types are constants on PlayerTimelineActions.Marker: SKIP_INTRO_MARKER=1,
 * SKIP_RECAP_MARKER=2. Auto-skip injects that same seek at the top of onMarkerHit, but ONLY for type
 * 1/2, so it never touches next-episode or ad markers. Seeking to the marker's own endTimeMillis puts
 * the playhead just past the window, so it does not re-fire. Names are not obfuscated.
 */
internal object OnMarkerHitFingerprint : Fingerprint(
    definingClass = "Lcom/zee5/android/ui/player/presentation/controls/PlaybackControlsFragment;",
    name = "onMarkerHit",
    returnType = "V",
    parameters = listOf("Lcom/zee5/android/ui/player/domain/models/PlayerTimelineActions;"),
)
