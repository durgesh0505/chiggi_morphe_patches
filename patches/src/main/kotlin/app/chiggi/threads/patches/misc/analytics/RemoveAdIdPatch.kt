package app.chiggi.threads.patches.misc.analytics

import app.chiggi.threads.patches.shared.Constants.COMPATIBILITY_THREADS
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Advertising-id permissions to strip so no component (the Play Services measurement SDK, Meta ad
 * SDKs) can read the device advertising id.
 *
 * Note: Threads uses Meta's own analytics stack (analytics2 / OneFabric / IG uploadscheduler), not
 * Firebase Analytics, so there is no boolean manifest flag to flip — this patch only removes the
 * AD_ID permissions. Core Meta telemetry is not disabled here (doing so reliably is high-risk and
 * out of scope).
 */
private val AD_ID_PERMISSIONS = setOf(
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
)

@Suppress("unused")
val removeAdIdPatch = resourcePatch(
    name = "Remove AD_ID permission",
    description = "Removes the advertising-id (AD_ID) permissions so the device advertising id " +
        "cannot be read for ad tracking. Does not disable Meta's core analytics.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_THREADS)

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
