package app.chiggi.vimtv.patches.misc.analytics

import app.chiggi.vimtv.patches.shared.Constants.COMPATIBILITY_VIMTV
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

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
        "device advertising id, Attribution and Topics signals cannot be read for ad tracking.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VIMTV)

    execute {
        document("AndroidManifest.xml").use { document ->
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
