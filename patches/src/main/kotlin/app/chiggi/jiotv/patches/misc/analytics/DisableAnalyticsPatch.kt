package app.chiggi.jiotv.patches.misc.analytics

import app.chiggi.jiotv.patches.shared.Constants.COMPATIBILITY_JIOTV
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

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
    compatibleWith(COMPATIBILITY_JIOTV)

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
