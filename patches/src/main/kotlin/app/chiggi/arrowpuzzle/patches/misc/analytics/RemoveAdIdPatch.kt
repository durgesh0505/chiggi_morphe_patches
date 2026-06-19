package app.chiggi.arrowpuzzle.patches.misc.analytics

import app.chiggi.arrowpuzzle.patches.shared.Constants.COMPATIBILITY_ARROWPUZZLE
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Advertising-id / Privacy Sandbox (Ad Services) permissions to strip so no SDK can read the device
 * advertising id or the Topics/Attribution signals for ad tracking.
 */
private val AD_ID_PERMISSIONS = setOf(
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    "android.permission.ACCESS_ADSERVICES_TOPICS",
)

@Suppress("unused")
val removeAdIdPatch = resourcePatch(
    name = "Remove AD_ID permission",
    description = "Removes the advertising-id and Ad Services (Privacy Sandbox) permissions so the " +
        "device advertising id, Topics and Attribution signals cannot be read for ad tracking.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ARROWPUZZLE)

    execute {
        document("AndroidManifest.xml").use { document ->
            // Iterate over a snapshot since removeChild mutates the live NodeList.
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
