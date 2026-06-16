package app.chiggi.sonyliv.patches.misc.branding

import app.chiggi.sonyliv.patches.shared.Constants.COMPATIBILITY_SONYLIV
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element

private const val LAUNCHER_ACTIVITY = "com.sonyliv.ui.splash.SplashActivity"

@Suppress("unused")
val changeAppNamePatch = resourcePatch(
    name = "Change app name",
    description = "Changes the app name shown under the launcher icon. " +
        "Set the desired name in the patch options.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_SONYLIV)

    val appName by stringOption(
        key = "appName",
        default = "Sony LIV",
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

            // Override the leanback launcher activity label so the TV launcher shows the new name.
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
