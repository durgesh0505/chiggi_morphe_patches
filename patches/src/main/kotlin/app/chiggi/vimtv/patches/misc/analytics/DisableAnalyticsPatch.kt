package app.chiggi.vimtv.patches.misc.analytics

import app.chiggi.vimtv.patches.shared.Constants.COMPATIBILITY_VIMTV
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Firebase/Google Analytics reads these boolean <meta-data> flags from the manifest to decide
 * whether to collect data and the advertising id. Setting them false disables Analytics, Crashlytics
 * and Performance collection without touching CleverTap (which is dependency-injected and drives
 * in-app UI, so disabling it would crash parts of the app).
 */
private val FIREBASE_DISABLE_FLAGS = mapOf(
    "firebase_analytics_collection_enabled" to "false",
    "google_analytics_adid_collection_enabled" to "false",
    "google_analytics_ssaid_collection_enabled" to "false",
    "google_analytics_default_allow_ad_personalization_signals" to "false",
    "firebase_crashlytics_collection_enabled" to "false",
    "firebase_performance_collection_enabled" to "false",
)

@Suppress("unused")
val disableAnalyticsPatch = resourcePatch(
    name = "Disable analytics",
    description = "Disables Firebase/Google Analytics, Crashlytics and Performance data collection " +
        "via manifest flags. CleverTap is left intact (it drives in-app UI and disabling it would " +
        "crash the app). Push notifications are unaffected.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VIMTV)

    execute {
        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as Element

            val existing = HashMap<String, Element>()
            val metaDataNodes = document.getElementsByTagName("meta-data")
            for (i in 0 until metaDataNodes.length) {
                val element = metaDataNodes.item(i) as Element
                existing[element.getAttribute("android:name")] = element
            }

            FIREBASE_DISABLE_FLAGS.forEach { (name, value) ->
                val current = existing[name]
                if (current != null) {
                    current.setAttribute("android:value", value)
                } else {
                    application.appendChild(
                        document.createElement("meta-data").apply {
                            setAttribute("android:name", name)
                            setAttribute("android:value", value)
                        },
                    )
                }
            }
        }
    }
}
