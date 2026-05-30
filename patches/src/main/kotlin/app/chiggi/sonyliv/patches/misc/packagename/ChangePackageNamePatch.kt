package app.chiggi.sonyliv.patches.misc.packagename

import app.chiggi.sonyliv.patches.shared.Constants.COMPATIBILITY_SONYLIV
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element

private const val ORIGINAL_PACKAGE = "com.sonyliv"

@Suppress("unused")
val changePackageNamePatch = resourcePatch(
    name = "Change package name",
    description = "Changes the app package name so the patched app installs alongside the " +
        "original. Set the desired package name in the patch options. Changing the package " +
        "name can cause unexpected issues with some app features.",
    default = false,
) {
    val packageName by stringOption(
        key = "packageName",
        default = "$ORIGINAL_PACKAGE.chiggi",
        title = "Package name",
        description = "The new application package name (e.g. com.sonyliv.chiggi).",
        required = true,
    ) {
        // Valid Android package name: dot-separated segments, each starting with a letter.
        it != null && it.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+$"))
    }

    finalize {
        document("AndroidManifest.xml").use { document ->
            val newPackage = packageName!!

            // Rename the package.
            document.documentElement.setAttribute("package", newPackage)

            // Rewrite provider authorities derived from the original package
            // (FileProvider, androidx-startup, CleverTap, Firebase, MobileAds) so they stay unique
            // and do not collide with the original app on install.
            val providers = document.getElementsByTagName("provider")
            for (i in 0 until providers.length) {
                val provider = providers.item(i) as Element
                val authorities = provider.getAttribute("android:authorities")
                if (authorities.startsWith("$ORIGINAL_PACKAGE.")) {
                    provider.setAttribute(
                        "android:authorities",
                        authorities.replace(ORIGINAL_PACKAGE, newPackage),
                    )
                }
            }
        }
    }
}
