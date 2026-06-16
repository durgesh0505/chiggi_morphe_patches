package app.chiggi.nutrilio.patches.misc.packagename

import app.chiggi.nutrilio.patches.shared.Constants.COMPATIBILITY_NUTRILIO
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element

private const val ORIGINAL_PACKAGE = "net.nutrilio"

@Suppress("unused")
val changePackageNamePatch = resourcePatch(
    name = "Change package name",
    description = "Changes the app package name so the patched app installs alongside the " +
        "original. Set the desired package name in the patch options. Changing the package " +
        "name can cause unexpected issues with some app features.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_NUTRILIO)

    val packageName by stringOption(
        key = "packageName",
        default = "$ORIGINAL_PACKAGE.morphe",
        title = "Package name",
        description = "The new application package name (e.g. net.nutrilio.morphe).",
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

            // Rewrite provider authorities derived from the original package (FileProvider,
            // androidx-startup, Firebase, etc.) so they stay unique and do not collide with the
            // original app on install.
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

            // Rename the app's own custom permissions (declared and used) so they do not clash with
            // the original app's signature-level permissions, which would otherwise cause
            // INSTALL_FAILED_DUPLICATE_PERMISSION ("conflicts with an existing package").
            // Only names under the original package prefix are touched; system/third-party
            // permissions (android.permission.*, com.google.*, etc.) are left unchanged.
            listOf("permission", "uses-permission").forEach { tag ->
                val nodes = document.getElementsByTagName(tag)
                for (i in 0 until nodes.length) {
                    val node = nodes.item(i) as Element
                    val name = node.getAttribute("android:name")
                    if (name.startsWith("$ORIGINAL_PACKAGE.") && !name.startsWith("$newPackage.")) {
                        node.setAttribute("android:name", name.replaceFirst("$ORIGINAL_PACKAGE.", "$newPackage."))
                    }
                }
            }
        }
    }
}
