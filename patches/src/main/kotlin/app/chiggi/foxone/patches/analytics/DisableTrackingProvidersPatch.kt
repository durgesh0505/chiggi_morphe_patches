package app.chiggi.foxone.patches.analytics

import app.chiggi.foxone.patches.shared.Constants.COMPATIBILITY_FOXONE
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

// Analytics/telemetry auto-init ContentProviders to disable so the SDKs never start.
private val DISABLE_PROVIDERS = setOf(
    "io.heap.core.HeapContentProvider",
    "io.heap.autocapture.HeapViewAutocaptureContentProvider",
    "io.heap.autocapture.compose.HeapComposeAutocaptureContentProvider",
    "com.fox.android.video.player.listener.logging.RemoteLoggingInitProvider",
)

// Firebase data-collection manifest flags.
private val FIREBASE_DISABLE_FLAGS = mapOf(
    "firebase_analytics_collection_enabled" to "false",
    "firebase_crashlytics_collection_enabled" to "false",
    "firebase_performance_collection_enabled" to "false",
    "google_analytics_adid_collection_enabled" to "false",
    "google_analytics_ssaid_collection_enabled" to "false",
)

@Suppress("unused")
val disableTrackingProvidersPatch = resourcePatch(
    name = "Disable tracking SDKs",
    description = "Stops the Heap analytics and FOX remote-logging SDKs from auto-initialising " +
        "(disables their startup ContentProviders) and turns off Firebase Analytics/Crashlytics/" +
        "Performance collection via manifest flags. Push notifications are unaffected.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_FOXONE)

    execute {
        document("AndroidManifest.xml").use { document ->
            // Disable the analytics auto-init providers.
            val providers = document.getElementsByTagName("provider")
            for (i in 0 until providers.length) {
                val provider = providers.item(i) as Element
                if (provider.getAttribute("android:name") in DISABLE_PROVIDERS) {
                    provider.setAttribute("android:enabled", "false")
                }
            }

            // Add/override Firebase collection flags.
            val application = document.getElementsByTagName("application").item(0) as Element
            val existing = HashMap<String, Element>()
            val metaData = document.getElementsByTagName("meta-data")
            for (i in 0 until metaData.length) {
                val element = metaData.item(i) as Element
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
