package app.chiggi.capcut.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.chiggi.capcut.patches.shared.Constants
import com.android.tools.smali.dexlib2.AccessFlags

private object GeoBlockConfigGetCertBlocksFingerprint : Fingerprint(
    definingClass = "Lcom/vega/core/privacy/fbv/geoblock/GeoBlockConfig;",
    name = "getCertBlocks",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/List;",
    parameters = listOf(),
)

private object GeoBlockConfigGetDockBlocksFingerprint : Fingerprint(
    definingClass = "Lcom/vega/core/privacy/fbv/geoblock/GeoBlockConfig;",
    name = "getDockBlocks",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/List;",
    parameters = listOf(),
)

private object GeoBlockConfigGetEntranceBlocksFingerprint : Fingerprint(
    definingClass = "Lcom/vega/core/privacy/fbv/geoblock/GeoBlockConfig;",
    name = "getEntranceBlocks",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/List;",
    parameters = listOf(),
)

private object GeoBlockConfigGetPageBlocksFingerprint : Fingerprint(
    definingClass = "Lcom/vega/core/privacy/fbv/geoblock/GeoBlockConfig;",
    name = "getPageBlocks",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/List;",
    parameters = listOf(),
)

private object GeoBlockConfigGetSchemeBlocksFingerprint : Fingerprint(
    definingClass = "Lcom/vega/core/privacy/fbv/geoblock/GeoBlockConfig;",
    name = "getSchemeBlocks",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/List;",
    parameters = listOf(),
)

@Suppress("unused")
val capcutBypassGeoBlockPatch = bytecodePatch(
    name = "Bypass GeoBlock Region Restriction",
    description = "Forces CapCut GeoBlock config lists (cert, dock, entrance, page, scheme) to empty so region-blocked surfaces stay enabled. Server-refused content may still fail to load.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)

    execute {
        var patched = 0

        fun forceEmptyList(fp: Fingerprint) {
            fp.method.addInstructions(
                0,
                """
                    invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                    move-result-object v0
                    return-object v0
                """,
            )
            patched++
        }

        forceEmptyList(GeoBlockConfigGetCertBlocksFingerprint)
        forceEmptyList(GeoBlockConfigGetDockBlocksFingerprint)
        forceEmptyList(GeoBlockConfigGetEntranceBlocksFingerprint)
        forceEmptyList(GeoBlockConfigGetPageBlocksFingerprint)
        forceEmptyList(GeoBlockConfigGetSchemeBlocksFingerprint)

        println("[Bypass GeoBlock Region Restriction] Applied $patched hooks -> GeoBlock lists forced empty.")
    }
}
