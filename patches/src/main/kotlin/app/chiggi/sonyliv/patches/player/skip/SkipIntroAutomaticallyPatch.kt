package app.chiggi.sonyliv.patches.player.skip

import app.chiggi.sonyliv.patches.shared.Constants.COMPATIBILITY_SONYLIV
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch

private const val SKIP_HELPER_CLASS =
    "Lcom/sonyliv/mediaplayer/player/playerrevamp/SLPlayerSkipHelper;"

@Suppress("unused")
val skipIntroAutomaticallyPatch = bytecodePatch(
    name = "Auto skip intro and recap",
    description = "Automatically skips the intro, recap and song segments as soon as the " +
        "'Skip' button would appear, without waiting for a tap.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SONYLIV)

    execute {
        // setSkipIntroVisibility(isVisible, skipBtnToUpdate, idlePlayerState) shows the Skip button
        // when isVisible == true. Instead of showing it, immediately invoke onClick(btnSkipIntro),
        // which runs the existing skip logic (analytics + skipHelperInterface.skipClick()).
        //
        // onClick() internally calls setSkipIntroVisibility(false, ...); that re-entry hits the
        // `if-eqz p1` guard below (isVisible == false) and is ignored, so there is no recursion.
        //
        // p0 = this, p1 = isVisible (Z), p2 = skipBtnToUpdate (String), p3 = idlePlayerState (Boolean)
        SetSkipIntroVisibilityFingerprint.method.addInstructionsWithLabels(
            0,
            """
                if-eqz p1, :original
                iget-object v0, p0, $SKIP_HELPER_CLASS->btnSkipIntro:Landroid/widget/Button;
                if-eqz v0, :original
                invoke-virtual { p0, v0 }, $SKIP_HELPER_CLASS->onClick(Landroid/view/View;)V
                return-void
                :original
                nop
            """,
        )
    }
}
