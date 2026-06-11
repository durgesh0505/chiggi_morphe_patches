package app.chiggi.sonyliv.patches.player.upnext

import app.chiggi.sonyliv.patches.shared.Constants.COMPATIBILITY_SONYLIV
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch

private const val NEXT_EPISODE_UI_CLASS =
    "Lcom/sonyliv/mediaplayer/player/playerrevamp/SLNextEpisodeUI;"
private const val BINDING_CLASS =
    "Lcom/sonyliv/databinding/LogixTvPlaybackNextEpisodeBinding;"

@Suppress("unused")
val autoSkipUpNextPatch = bytecodePatch(
    name = "Auto-skip Up Next",
    description = "Plays the next episode immediately when the \"Up Next\" card appears, " +
        "skipping the auto-play countdown.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SONYLIV)

    execute {
        // When the Up Next card becomes visible, immediately invoke the next-episode button's
        // click listener (the same action the countdown would trigger) so the next episode
        // plays at once. p0 = this, p1 = shouldBeVisible (Z).
        UpdateNextEpisodeVisibilityFingerprint.method.addInstructionsWithLabels(
            0,
            """
                if-eqz p1, :original
                iget-object v0, p0, $NEXT_EPISODE_UI_CLASS->nextEpisodeBinding:$BINDING_CLASS
                if-eqz v0, :original
                iget-object v1, v0, $BINDING_CLASS->nextEpisodeBtn:Landroid/widget/LinearLayout;
                iget-object v2, p0, $NEXT_EPISODE_UI_CLASS->clickListener:Landroid/view/View${'$'}OnClickListener;
                invoke-interface { v2, v1 }, Landroid/view/View${'$'}OnClickListener;->onClick(Landroid/view/View;)V
                return-void
                :original
                nop
            """,
        )
    }
}
