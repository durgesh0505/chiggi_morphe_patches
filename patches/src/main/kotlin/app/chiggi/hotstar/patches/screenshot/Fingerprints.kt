package app.chiggi.hotstar.patches.screenshot

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

/**
 * Screenshot/FLAG_SECURE fingerprints. Approach credited to Paresh-Maheshwari (GPL-3.0).
 */

// ABConfig.getUseSecureView() — stable non-obfuscated class.
internal object UseSecureViewFingerprint : Fingerprint(
    definingClass = "Lcom/hotstar/player/models/config/ABConfig;",
    name = "getUseSecureView",
    returnType = "Z",
)

// Compose helper that adds FLAG_SECURE (0x2000) to the Activity window.
internal object ComposeFlagSecureFingerprint : Fingerprint(
    returnType = "V",
    filters = listOf(
        string("null cannot be cast to non-null type android.app.Activity"),
        methodCall(definingClass = "Landroid/app/Activity;", name = "getWindow"),
        literal(0x2000),
        methodCall(definingClass = "Landroid/view/Window;", name = "addFlags"),
    ),
)
