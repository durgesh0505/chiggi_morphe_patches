package app.chiggi.capcut.patches

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.chiggi.capcut.patches.shared.Constants
import app.chiggi.capcut.patches.shared.LocaleUtils

@Suppress("unused")
val capcutLocaleSlimmerPatch = resourcePatch(
    name = "Locale Assets Slimmer",
    description = "Strips unselected CapCut asset locale bundles (assets/locales/*.json) from the APK. English is always preserved. For res/values-* string tables use the canonical universal Remove Extra Languages patch.",
    default = false,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)

    val targetLocales by stringOption(
        key = "locales",
        title = "Locales to keep",
        description = "Comma-separated language codes to preserve (e.g. 'en, es, pt, fr, de'). Base fallback and English are always retained.",
        default = "en",
        required = false,
    )

    execute {
        val assetsLocalesDir = try {
            get("assets/locales")
        } catch (_: Throwable) {
            null
        }

        if (assetsLocalesDir == null || !assetsLocalesDir.exists() || !assetsLocalesDir.isDirectory) {
            println("[CapCut Locale Assets Slimmer] Skipped: assets/locales directory not found.")
            return@execute
        }

        val selectedLocales = LocaleUtils.parseTargetLocales(targetLocales, defaultLocales = setOf("en", "en-us"))

        var savedBytes = 0L
        var strippedAssetFilesCount = 0

        // CapCut Asset Localization Bundles (assets/locales/*.json).
        // res/values-* string tables are intentionally not touched here:
        // they are covered by the canonical universal Remove Extra Languages patch.
        val jsonFiles = assetsLocalesDir.listFiles { f -> f.isFile && f.extension.equals("json", ignoreCase = true) }
        if (jsonFiles != null) {
            for (file in jsonFiles) {
                val baseName = file.nameWithoutExtension.lowercase()

                // English fallback is strictly preserved
                if (baseName == "en" || baseName.startsWith("en-") || baseName.startsWith("en_")) {
                    continue
                }

                // Extract base language prefix (e.g. "es-419" -> "es", "ar-arab" -> "ar")
                val langPrefix = baseName.substringBefore("-").substringBefore("_")
                if (baseName in selectedLocales || langPrefix in selectedLocales) {
                    continue
                }

                val fileSize = file.length()
                if (file.delete()) {
                    savedBytes += fileSize
                    strippedAssetFilesCount++
                }
            }
        }

        if (strippedAssetFilesCount == 0) {
            println("[CapCut Locale Assets Slimmer] No unselected asset locale files found to strip (retained locales: ${selectedLocales.sorted().joinToString(", ")}).")
            return@execute
        }

        val savedFormatted = LocaleUtils.formatBytes(savedBytes)
        println("[CapCut Locale Assets Slimmer] Stripped $strippedAssetFilesCount unselected asset locale files -> Saved $savedFormatted (retained locales: ${selectedLocales.sorted().joinToString(", ")})")
    }
}
