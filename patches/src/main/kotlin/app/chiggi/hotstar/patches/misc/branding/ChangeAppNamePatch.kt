package app.chiggi.hotstar.patches.misc.branding

import app.chiggi.hotstar.patches.shared.Constants.COMPATIBILITY_HOTSTAR
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element

private const val LAUNCHER_ACTIVITY = "com.hotstar.MainActivity"

@Suppress("unused")
val changeAppNamePatch = resourcePatch(
    name = "Change app name",
    description = "Changes the app name shown under the launcher icon. " +
        "Set the desired name in the patch options.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_HOTSTAR)

    val appName by stringOption(
        key = "appName",
        default = "JioHotstar Morphe",
        title = "App name",
        description = "The name shown under the app icon.",
        required = true,
    )

    execute {
        document("AndroidManifest.xml").use { document ->
            val newName = appName!!

            // Override the <application> label.
            val application = document.getElementsByTagName("application").item(0) as Element
            application.setAttribute("android:label", newName)

            // Override the launcher activity label so the home screen shows the new name.
            val activities = document.getElementsByTagName("activity")
            for (i in 0 until activities.length) {
                val activity = activities.item(i) as Element
                if (activity.getAttribute("android:name") == LAUNCHER_ACTIVITY) {
                    activity.setAttribute("android:label", newName)
                }
            }
        }
    }
}
