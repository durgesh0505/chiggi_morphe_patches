package app.chiggi.vimtv.patches.misc.branding

import app.chiggi.vimtv.patches.shared.Constants.COMPATIBILITY_VIMTV
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element

private const val LAUNCHER_ACTIVITY = "com.yupptv.ott.ui.activity.SplashActivity"

@Suppress("unused")
val changeAppNamePatch = resourcePatch(
    name = "Change app name",
    description = "Changes the app name shown under the launcher icon. " +
        "Set the desired name in the patch options.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_VIMTV)

    val appName by stringOption(
        key = "appName",
        default = "Vi Movies and TV Morphe",
        title = "App name",
        description = "The name shown under the app icon.",
        required = true,
    )

    execute {
        document("AndroidManifest.xml").use { document ->
            val newName = appName!!

            val application = document.getElementsByTagName("application").item(0) as Element
            application.setAttribute("android:label", newName)

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
