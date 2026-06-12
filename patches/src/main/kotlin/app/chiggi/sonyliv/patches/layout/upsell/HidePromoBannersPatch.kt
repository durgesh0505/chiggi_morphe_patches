package app.chiggi.sonyliv.patches.layout.upsell

import app.chiggi.sonyliv.patches.shared.Constants.COMPATIBILITY_SONYLIV
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

@Suppress("unused")
val hidePromoBannersPatch = bytecodePatch(
    name = "Hide promo banners",
    description = "Hides the CleverTap-driven subscribe/premium promo banners and promo trays on " +
        "the home screen. Content rows and CleverTap pop-ups/overlays are not affected.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SONYLIV)

    execute {
        // Force the home-screen getters for the CleverTap promo tray and banner card to return
        // null, so neither promo row is rendered.
        val returnNull = """
            const/4 v0, 0x0
            return-object v0
        """

        GetCTListRowDataFingerprint.method.addInstructions(0, returnNull)
        GetCTBannerRowDataFingerprint.method.addInstructions(0, returnNull)
    }
}
