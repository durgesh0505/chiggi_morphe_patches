package app.chiggi.vimtv.patches.premium

import app.chiggi.vimtv.patches.shared.Constants.COMPATIBILITY_VIMTV
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val premiumUnlockPatch = bytecodePatch(
    name = "Premium unlock (UI)",
    description = "Forces the client 'subscribed' flags true so premium UI state shows unlocked. " +
        "OFF by default: Vi Movies and TV enforces entitlement server-side with Widevine DRM, so " +
        "this does NOT grant content you are not entitled to — it only changes UI, and content that " +
        "appears playable may still be refused by the server. Cosmetic UI change only.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VIMTV)

    execute {
        // Guarded: these are model getters that may vary; a missing one must not fail the patch.
        runCatching {
            ContentInfoIsSubscribedFingerprint.method.addInstructions(0, """
                const/4 v0, 0x1
                return v0
            """)
        }
        runCatching {
            PackageIsSubscribedFingerprint.method.addInstructions(0, """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """)
        }
    }
}
