package app.chiggi.foxone.patches.packagename

import app.chiggi.foxone.patches.shared.Constants.COMPATIBILITY_FOXONE
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import org.w3c.dom.Element

private const val ORIGINAL_PACKAGE = "com.fox.foxone"

@Suppress("unused")
val changePackageNamePatch = resourcePatch(
    name = "Change package name",
    description = "Changes the app package name so the patched app installs alongside the " +
        "original. Rewrites provider authorities and custom permissions. Set the desired " +
        "package name in the patch options. Changing the package name can cause unexpected " +
        "issues with some app features.",
    default = false,
) {
    val packageName by stringOption(
        key = "packageName",
        default = "$ORIGINAL_PACKAGE.morphe",
        title = "Package name",
        description = "The new application package name (e.g. com.fox.foxone.morphe).",
        required = true,
    ) {
        it != null && it.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+$"))
    }

    finalize {
        document("AndroidManifest.xml").use { document ->
            val newPackage = packageName!!

            document.documentElement.setAttribute("package", newPackage)

            // Rewrite provider authorities derived from the original package
            // (FileProvider, androidx-startup, Penthera/Virtuoso, Heap, Firebase, etc.).
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

            // Rename the app's own custom permissions (declared + used) so they do not clash with
            // the original app's signature-level permissions (INSTALL_FAILED_DUPLICATE_PERMISSION).
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
