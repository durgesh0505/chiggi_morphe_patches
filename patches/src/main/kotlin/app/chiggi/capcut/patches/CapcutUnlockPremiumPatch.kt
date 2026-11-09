package app.chiggi.capcut.patches

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.chiggi.capcut.patches.shared.Constants
import com.android.tools.smali.dexlib2.AccessFlags

private object SubscribeImplIsVipFingerprint : Fingerprint(
    definingClass = "Lcom/vega/subscribe/SubscribeImpl;",
    name = "isVip",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

private object PayVipImplIsVipFingerprint : Fingerprint(
    definingClass = "Lcom/lemon/editor/proxy/PayVipImpl;",
    name = "isVip",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

private object UserVipInfoIsVipUserFingerprint : Fingerprint(
    definingClass = "Lcom/lm/components/subscribe/config/UserVipInfo;",
    name = "isVipUser",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

private object UserVipInfoGetFlagFingerprint : Fingerprint(
    definingClass = "Lcom/lm/components/subscribe/config/UserVipInfo;",
    name = "getFlag",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

private object UserDetailInfoIsVipUserFingerprint : Fingerprint(
    definingClass = "Lcom/lemon/lv/clipmonetize/data/UserDetailInfo;",
    name = "isVipUser",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

private object UserDetailInfoGetFlagFingerprint : Fingerprint(
    definingClass = "Lcom/lemon/lv/clipmonetize/data/UserDetailInfo;",
    name = "getFlag",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

private object VipUserServiceImplIsVipFingerprint : Fingerprint(
    definingClass = "Lcom/vega/subscription/legacy/service/VipUserServiceImpl;",
    name = "isVip",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

private object SubscribeCloudImplIsVipFingerprint : Fingerprint(
    definingClass = "Lcom/lemon/editor/proxy/SubscribeCloudImpl;",
    name = "isVip",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

private object BusinessManagerBFingerprint : Fingerprint(
    definingClass = "Lcom/lemon/lv/clipmonetize/wrapper/BusinessManager;",
    name = "B",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

private object BusinessManagerYFingerprint : Fingerprint(
    definingClass = "Lcom/lemon/lv/clipmonetize/wrapper/BusinessManager;",
    name = "y",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
)

private object BusinessManagerXFingerprint : Fingerprint(
    definingClass = "Lcom/lemon/lv/clipmonetize/wrapper/BusinessManager;",
    name = "x",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Z"),
)

private object BusinessManagerVFingerprint : Fingerprint(
    definingClass = "Lcom/lemon/lv/clipmonetize/wrapper/BusinessManager;",
    name = "v",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Lcom/lemon/lv/clipmonetize/wrapper/BenefitResource;"),
)

private object BusinessManagerAFingerprint : Fingerprint(
    definingClass = "Lcom/lemon/lv/clipmonetize/wrapper/BusinessManager;",
    name = "A",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Lcom/lemon/lv/clipmonetize/wrapper/BenefitResource;"),
)

private object BusinessManagerTFingerprint : Fingerprint(
    definingClass = "Lcom/lemon/lv/clipmonetize/wrapper/BusinessManager;",
    name = "T",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Lcom/lemon/lv/clipmonetize/wrapper/BenefitResource;", "Z"),
)

private object VipExportUpsellJ3Fingerprint : Fingerprint(
    definingClass = "Lcom/vega/subscription/biz/function/VipExportBusinessFunction;",
    name = "j3", returnType = "Z", parameters = listOf(),
)
private object VipExportUpsellW0Fingerprint : Fingerprint(
    definingClass = "Lcom/vega/subscription/biz/function/VipExportBusinessFunction;",
    name = "w0", returnType = "Z", parameters = listOf(),
)

@Suppress("unused")
val capcutUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Ported from kveld9/kveld-extra-morphe-patches (GPLv3). Forces CapCut VIP gates to return true (SubscribeImpl, PayVipImpl, UserVipInfo, UserDetailInfo, VipUserServiceImpl, SubscribeCloudImpl) plus central account status flags (BusinessManager). Cloud-gated assets may still fail.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)

    execute {
        var patched = 0

        fun forceTrue(fp: Fingerprint) {
            fp.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
            patched++
        }

        fun forceFalse(fp: Fingerprint) {
            fp.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """,
            )
            patched++
        }

        forceTrue(SubscribeImplIsVipFingerprint)
        forceTrue(PayVipImplIsVipFingerprint)
        forceTrue(UserVipInfoIsVipUserFingerprint)
        forceTrue(UserVipInfoGetFlagFingerprint)
        forceTrue(UserDetailInfoIsVipUserFingerprint)
        forceTrue(UserDetailInfoGetFlagFingerprint)
        forceTrue(VipUserServiceImplIsVipFingerprint)
        forceTrue(SubscribeCloudImplIsVipFingerprint)
        forceTrue(BusinessManagerBFingerprint)
        forceTrue(BusinessManagerYFingerprint)
        forceTrue(BusinessManagerXFingerprint)
        forceTrue(BusinessManagerVFingerprint)
        forceTrue(BusinessManagerAFingerprint)
        forceTrue(BusinessManagerTFingerprint)
        forceFalse(VipExportUpsellJ3Fingerprint)
        forceFalse(VipExportUpsellW0Fingerprint)

        println("[Unlock Premium] Applied $patched hooks -> VIP gates forced true and export upsell gates forced false.")
    }
}
