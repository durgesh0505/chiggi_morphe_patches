package app.chiggi.zee5.patches.player.watermark

import app.morphe.patcher.Fingerprint

/**
 * com.zee5.android.ui.player.presentation.watermark.WaterMarkViewModel#updateLogoVisibility(boolean).
 *
 * The player watermark ("ZEE5" / channel logo overlaid on the video) is driven by the
 * `_logoVisibility` LiveData<Boolean>; WaterMarkFragment observes `showWaterMarkLogo()` (which returns
 * that LiveData) to show/hide the logo ImageView. `updateLogoVisibility(logoVisibility)` is the only
 * setter (`_logoVisibility.postValue(logoVisibility)`). Forcing the argument to false makes the logo
 * never become visible. Class and method names are not obfuscated, so this anchors on definingClass +
 * name + parameter.
 */
internal object UpdateLogoVisibilityFingerprint : Fingerprint(
    definingClass = "Lcom/zee5/android/ui/player/presentation/watermark/WaterMarkViewModel;",
    name = "updateLogoVisibility",
    returnType = "V",
    parameters = listOf("Z"),
)
