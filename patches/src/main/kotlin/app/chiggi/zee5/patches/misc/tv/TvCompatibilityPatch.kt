package app.chiggi.zee5.patches.misc.tv

import app.chiggi.zee5.patches.shared.Constants.COMPATIBILITY_ZEE5
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

// The ZEE5 TV build declares these <uses-feature> as required. Chromecast with Google TV / most
// Android TV boxes have no TV tuner, so they do NOT expose `android.software.live_tv`; with it marked
// required the installer rejects the app as "not compatible with your TV". Marking it (and leanback,
// for safety) optional makes it install on any TV/streamer. All other phone-only features in this APK
// are already required="false".
private val FEATURES_TO_MAKE_OPTIONAL = setOf(
    "android.software.live_tv",
    "android.software.leanback",
)

@Suppress("unused")
val tvCompatibilityPatch = resourcePatch(
    name = "Make installable on TV",
    description = "Makes the ZEE5 APK install on Android TV / Chromecast with Google TV. The build " +
        "marks android.software.live_tv as required, but streamer devices without a TV tuner do not " +
        "have that feature, so the installer reports \"not compatible with your TV\". This marks " +
        "live_tv and leanback optional so it installs everywhere; the leanback launcher entry is kept, " +
        "so it still appears on the TV home screen.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZEE5)

    execute {
        document("AndroidManifest.xml").use { document ->
            val featureNodes = document.getElementsByTagName("uses-feature")
            for (i in 0 until featureNodes.length) {
                val feature = featureNodes.item(i) as Element
                if (feature.getAttribute("android:name") in FEATURES_TO_MAKE_OPTIONAL) {
                    feature.setAttribute("android:required", "false")
                }
            }
        }
    }
}
