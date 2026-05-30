package app.chiggi.sonyliv.patches.misc.analytics

import app.chiggi.sonyliv.patches.shared.Constants.COMPATIBILITY_SONYLIV
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Firebase reads these boolean <meta-data> flags from the manifest to decide whether to collect
 * data. Setting them to false disables Analytics, Crashlytics, Performance and advertising-id
 * collection without touching push messaging (Cloud Messaging auto-init is intentionally left on).
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
val disableFirebaseTrackingPatch = resourcePatch(
    name = "Disable Firebase tracking",
    description = "Disables Firebase Analytics, Crashlytics and Performance data collection via " +
        "manifest flags. Push notifications are unaffected.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_SONYLIV)

    execute {
        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as Element

            // Index existing <meta-data android:name=...> so we update rather than duplicate.
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
