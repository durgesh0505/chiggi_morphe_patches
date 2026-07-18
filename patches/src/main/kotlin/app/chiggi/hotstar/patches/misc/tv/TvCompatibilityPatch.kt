package app.chiggi.hotstar.patches.misc.tv

import app.chiggi.hotstar.patches.shared.Constants.COMPATIBILITY_HOTSTAR
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

// Features this (phone) build declares as required that a TV/streamer device does not have, so the
// installer rejects the app as "not compatible with your TV". Marking them optional lets it install.
private val FEATURES_TO_MAKE_OPTIONAL = setOf(
    "android.hardware.touchscreen",
    "android.hardware.faketouch",
    "android.hardware.screen.portrait",
    "android.hardware.screen.landscape",
    "android.hardware.location",
    "android.hardware.location.gps",
    "android.hardware.location.network",
    "android.hardware.telephony",
    "android.hardware.wifi",
)

private const val LEANBACK_FEATURE = "android.software.leanback"
private const val LAUNCHER_CATEGORY = "android.intent.category.LAUNCHER"
private const val LEANBACK_LAUNCHER_CATEGORY = "android.intent.category.LEANBACK_LAUNCHER"

@Suppress("unused")
val tvCompatibilityPatch = resourcePatch(
    name = "Make installable on TV",
    description = "For PHONE builds only: makes a phone APK installable on Android TV / Google TV by " +
        "marking touchscreen, portrait-screen and other phone-only features optional (they are why " +
        "the TV installer reports \"not compatible with your TV\"), declaring the leanback feature " +
        "optional, and adding the leanback launcher category. Off by default because the JioHotstar " +
        "TV build (.apkm) is already a native leanback D-pad app and does not need it. Note: a phone " +
        "build patched this way still shows a portrait touch UI — a mouse or air-remote may be " +
        "needed since it expects touch, not a D-pad.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_HOTSTAR)

    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement

            // 1) Mark existing phone-only required features as optional, and record which ones are
            //    present as explicit elements.
            val seenFeatures = HashSet<String>()
            val featureNodes = document.getElementsByTagName("uses-feature")
            for (i in 0 until featureNodes.length) {
                val feature = featureNodes.item(i) as Element
                val name = feature.getAttribute("android:name")
                seenFeatures.add(name)
                if (name == LEANBACK_FEATURE || name in FEATURES_TO_MAKE_OPTIONAL) {
                    feature.setAttribute("android:required", "false")
                }
            }

            // 2) Add explicit optional <uses-feature> overrides for any target feature that is only
            //    IMPLIED (portrait from activity orientation, location/wifi from permissions), plus
            //    the leanback feature. An explicit required="false" overrides aapt's implied
            //    requirement, which is what actually gates the TV installer.
            (FEATURES_TO_MAKE_OPTIONAL + LEANBACK_FEATURE).forEach { name ->
                if (name !in seenFeatures) {
                    manifest.appendChild(
                        document.createElement("uses-feature").apply {
                            setAttribute("android:name", name)
                            setAttribute("android:required", "false")
                        },
                    )
                }
            }

            // 3) Add the leanback launcher category to every launcher intent-filter so the app
            //    appears on the TV home screen. Iterate over a snapshot of the launcher filters
            //    first, since appendChild mutates the live NodeList.
            val intentFilters = document.getElementsByTagName("intent-filter")
            val launcherFilters = ArrayList<Element>()
            for (i in 0 until intentFilters.length) {
                val filter = intentFilters.item(i) as Element
                var hasLauncher = false
                var hasLeanbackLauncher = false
                val categories = filter.getElementsByTagName("category")
                for (j in 0 until categories.length) {
                    when ((categories.item(j) as Element).getAttribute("android:name")) {
                        LAUNCHER_CATEGORY -> hasLauncher = true
                        LEANBACK_LAUNCHER_CATEGORY -> hasLeanbackLauncher = true
                    }
                }
                if (hasLauncher && !hasLeanbackLauncher) {
                    launcherFilters.add(filter)
                }
            }
            launcherFilters.forEach { filter ->
                filter.appendChild(
                    document.createElement("category").apply {
                        setAttribute("android:name", LEANBACK_LAUNCHER_CATEGORY)
                    },
                )
            }
        }
    }
}
