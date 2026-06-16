package app.chiggi.nutrilio.patches.misc.analytics

import app.chiggi.nutrilio.patches.shared.Constants.COMPATIBILITY_NUTRILIO
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Firebase/Google Analytics reads these boolean <meta-data> flags from the manifest to decide
 * whether to collect data and the advertising id. Setting them to false disables Analytics and the
 * AD_ID collection without touching push messaging.
 */
private val FIREBASE_DISABLE_FLAGS = mapOf(
    "firebase_analytics_collection_enabled" to "false",
    "google_analytics_adid_collection_enabled" to "false",
    "google_analytics_ssaid_collection_enabled" to "false",
    "google_analytics_default_allow_ad_personalization_signals" to "false",
    "firebase_crashlytics_collection_enabled" to "false",
    "firebase_performance_collection_enabled" to "false",
)

/**
 * Advertising-id permissions to strip so no component (including the Play Services measurement SDK)
 * can read the device advertising id.
 */
private val AD_ID_PERMISSIONS = setOf(
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
)

@Suppress("unused")
val disableAnalyticsPatch = resourcePatch(
    name = "Disable analytics",
    description = "Disables Firebase/Google Analytics, Crashlytics and Performance data collection " +
        "via manifest flags and removes the advertising-id (AD_ID) permissions. Push " +
        "notifications are unaffected.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_NUTRILIO)

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

            // Remove the advertising-id permissions (iterate over a snapshot since removeChild
            // mutates the live NodeList).
            val permissionNodes = document.getElementsByTagName("uses-permission")
            val toRemove = ArrayList<Element>()
            for (i in 0 until permissionNodes.length) {
                val element = permissionNodes.item(i) as Element
                if (element.getAttribute("android:name") in AD_ID_PERMISSIONS) {
                    toRemove.add(element)
                }
            }
            toRemove.forEach { it.parentNode.removeChild(it) }
        }
    }
}
