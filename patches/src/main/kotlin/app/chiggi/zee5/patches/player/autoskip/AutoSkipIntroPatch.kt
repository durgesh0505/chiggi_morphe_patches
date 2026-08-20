package app.chiggi.zee5.patches.player.autoskip

import app.chiggi.zee5.patches.shared.Constants.COMPATIBILITY_ZEE5
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val autoSkipIntroPatch = bytecodePatch(
    name = "Auto-skip intro and recap",
    description = "Automatically skips the intro and recap of a show instead of waiting for you to press " +
        "the Skip button. When the player reaches an intro (SKIP_INTRO_MARKER=1) or recap " +
        "(SKIP_RECAP_MARKER=2) marker, it does exactly what tapping the Skip button does — sets " +
        "seek-exact and seeks straight to that marker's end. Seek-exact is required: without it the " +
        "seek snaps to a keyframe before the intro end, which lands back inside the marker window and " +
        "loops. It only acts on intro/recap markers, never on next-episode or ad markers. Disable this " +
        "if a title's markers are wrong and it skips too far.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZEE5)

    execute {
        // p0 = fragment, p1 = PlayerTimelineActions action. At method entry v0/v1/v2 are free temps
        // (onMarkerHit reassigns them before its own use); p1 is only read, never written.
        OnMarkerHitFingerprint.method.addInstructionsWithLabels(
            0,
            """
                instance-of v0, p1, Lcom/zee5/android/ui/player/domain/models/PlayerTimelineActions${'$'}Marker;
                if-eqz v0, :zee_no_skip
                move-object v0, p1
                check-cast v0, Lcom/zee5/android/ui/player/domain/models/PlayerTimelineActions${'$'}Marker;
                invoke-virtual { v0 }, Lcom/zee5/android/ui/player/domain/models/PlayerTimelineActions${'$'}Marker;->getMarkerType()I
                move-result v0
                const/4 v1, 0x1
                if-eq v0, v1, :zee_do_skip
                const/4 v1, 0x2
                if-ne v0, v1, :zee_no_skip
                :zee_do_skip
                invoke-virtual { p0 }, Lcom/zee5/android/ui/player/presentation/controls/PlaybackControlsFragment;->getZMediaSession()Lcom/zee5/android/ui/player/mediasession/ZMediaSession;
                move-result-object v0
                const/4 v1, 0x1
                invoke-virtual { v0, v1 }, Lcom/zee5/android/ui/player/mediasession/ZMediaSession;->setSeekExact(Z)V
                invoke-virtual { p0 }, Lcom/zee5/android/ui/player/presentation/controls/PlaybackControlsFragment;->getMediaController()Landroid/support/v4/media/session/MediaControllerCompat;
                move-result-object v0
                invoke-virtual { v0 }, Landroid/support/v4/media/session/MediaControllerCompat;->getTransportControls()Landroid/support/v4/media/session/MediaControllerCompat${'$'}TransportControls;
                move-result-object v0
                invoke-virtual { p1 }, Lcom/zee5/android/ui/player/domain/models/PlayerTimelineActions;->getEndTimeMillis()J
                move-result-wide v1
                invoke-virtual { v0, v1, v2 }, Landroid/support/v4/media/session/MediaControllerCompat${'$'}TransportControls;->seekTo(J)V
                return-void
                :zee_no_skip
                nop
            """,
        )
    }
}
