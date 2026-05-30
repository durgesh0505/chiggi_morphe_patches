package app.chiggi.sonyliv.patches.player.skip

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.sonyliv.mediaplayer.player.playerrevamp.SLPlayerSkipHelper
 *
 * #setSkipIntroVisibility(boolean isVisible, String skipBtnToUpdate, Boolean idlePlayerState)
 * Shows/hides the "Skip Intro" / "Skip Song" / "Skip Recap" button. When called with
 * isVisible = true the button is shown; the user then taps it and onClick() performs the skip.
 * Class/method names are not obfuscated in 6.23.1.
 */
internal object SetSkipIntroVisibilityFingerprint : Fingerprint(
    name = "setSkipIntroVisibility",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Z", "Ljava/lang/String;", "Ljava/lang/Boolean;"),
    definingClass = "Lcom/sonyliv/mediaplayer/player/playerrevamp/SLPlayerSkipHelper;",
)
