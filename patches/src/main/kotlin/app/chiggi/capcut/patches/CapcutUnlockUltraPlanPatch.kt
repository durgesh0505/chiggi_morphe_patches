package app.chiggi.capcut.patches

import app.chiggi.capcut.patches.shared.Constants
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags

private const val USER_DETAIL_INFO = "Lcom/lemon/lv/clipmonetize/data/UserDetailInfo;"
private const val USER_VIP_INFO = "Lcom/lm/components/subscribe/config/UserVipInfo;"
private const val API_MODELS_EXT = "Lcom/lemon/lv/clipmonetize/data/APIModelsExtKt;"
private const val FAR_FUTURE_UNIX_SECONDS = "0x3afff4417fL" // 9999-12-31 23:59:59 UTC

private fun userDetailGetter(name: String, returnType: String) = Fingerprint(
    definingClass = USER_DETAIL_INFO,
    name = name,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = returnType,
    parameters = listOf(),
)

private fun userVipGetter(name: String, returnType: String) = Fingerprint(
    definingClass = USER_VIP_INFO,
    name = name,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = returnType,
    parameters = listOf(),
)

private object SubscriptionLevelFingerprint : Fingerprint(
    definingClass = API_MODELS_EXT,
    name = "getSubscriptionLevel",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Lcom/lemon/lv/clipmonetize/data/SubscriptionLevel;",
    parameters = listOf(USER_DETAIL_INFO),
)

private object SubscriptionEndTimeFingerprint : Fingerprint(
    definingClass = API_MODELS_EXT,
    name = "getSubscriptionEndTime",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "J",
    parameters = listOf(USER_DETAIL_INFO, "Lcom/lemon/lv/clipmonetize/data/SubscriptionLevel;"),
)

private object SubscriptionNextRenewalFingerprint : Fingerprint(
    definingClass = API_MODELS_EXT,
    name = "getNextRenewalTime",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "J",
    parameters = listOf(USER_DETAIL_INFO, "Lcom/lemon/lv/clipmonetize/data/SubscriptionLevel;"),
)

private val CurrentVipLevel = userDetailGetter("getCurVipLevel", "Ljava/lang/String;")
private val ExpiredVipLevel = userDetailGetter("getExpiredVipLevel", "Ljava/lang/String;")
private val SubscribeType = userDetailGetter("getSubscribeType", "Ljava/lang/String;")
private val IsCancelSubscribe = userDetailGetter("isCancelSubscribe", "Z")
private val DidHadVip = userDetailGetter("getDidHadVip", "Z")
private val IsFirstSubscribe = userDetailGetter("isFirstSubscribe", "Z")
private val IsFreeTrial = userDetailGetter("isFreeTrial", "Z")
private val FreeTrial = userDetailGetter("getFreeTrial", "Z")
private val IsPippitFreeTrial = userDetailGetter("isPippitFreeTrial", "Z")
private val ProtectExpiring = userDetailGetter("getProtectExpiring", "Z")
private val IapUncancelledExpired = userDetailGetter("isIapUncancelledExpiredSubscribe", "Z")
private val NextRenewalTime = userDetailGetter("getNextRenewalTime", "J")

private val FarFutureDetailTimes = listOf(
    userDetailGetter("getEndTime", "J"),
    userDetailGetter("getVipRealEnd", "J"),
    userDetailGetter("getSvipEndTime", "J"),
    userDetailGetter("getBenefitEndAt", "J"),
    userDetailGetter("getProtectEndTime", "J"),
)

private val LegacyEndTime = userVipGetter("getEndTime", "Ljava/lang/String;")
private val LegacyEndTimeSnakeCase = userVipGetter("getEnd_time", "Ljava/lang/String;")
private val LegacySubscribeType = userVipGetter("getSubscribe_type", "Ljava/lang/String;")
private val LegacyCancelSubscribe = userVipGetter("isCancelSubscribe", "Z")
private val LegacyCancelSubscribeSnakeCase = userVipGetter("is_cancel_subscribe", "Z")
private val LegacyFirstSubscribe = userVipGetter("isFirstSubscribe", "Z")
private val LegacyFirstSubscribeSnakeCase = userVipGetter("is_first_subscribe", "Z")

@Suppress("unused")
val capcutUnlockUltraPlanPatch = bytecodePatch(
    name = "Unlock Ultra Plan",
    description = "Reports CapCut Ultra as the active locally purchased tier, sets a non-expiring local benefit end date, and disables renewal, cancellation, trial, and expiry-warning states. Server-side credits and cloud entitlements are not granted.",
    default = true,
) {
    compatibleWith(Constants.COMPATIBILITY_CAPCUT)
    dependsOn(capcutUnlockPremiumPatch)

    execute {
        var patched = 0

        fun forceString(fp: Fingerprint, value: String) {
            fp.method.addInstructions(0, "const-string v0, \"$value\"\nreturn-object v0")
            patched++
        }

        fun forceBoolean(fp: Fingerprint, value: Boolean) {
            val literal = if (value) "0x1" else "0x0"
            fp.method.addInstructions(0, "const/4 v0, $literal\nreturn v0")
            patched++
        }

        fun forceLong(fp: Fingerprint, literal: String) {
            fp.method.addInstructions(0, "const-wide v0, $literal\nreturn-wide v0")
            patched++
        }

        forceString(CurrentVipLevel, "ultra")
        forceString(ExpiredVipLevel, "")
        forceString(SubscribeType, "un-auto")
        forceBoolean(IsCancelSubscribe, false)
        forceBoolean(DidHadVip, true)
        forceBoolean(IsFirstSubscribe, false)
        forceBoolean(IsFreeTrial, false)
        forceBoolean(FreeTrial, false)
        forceBoolean(IsPippitFreeTrial, false)
        forceBoolean(ProtectExpiring, false)
        forceBoolean(IapUncancelledExpired, false)
        forceLong(NextRenewalTime, "0x0")
        FarFutureDetailTimes.forEach { forceLong(it, FAR_FUTURE_UNIX_SECONDS) }

        forceString(LegacyEndTime, "253402300799")
        forceString(LegacyEndTimeSnakeCase, "253402300799")
        forceString(LegacySubscribeType, "un-auto")
        forceBoolean(LegacyCancelSubscribe, false)
        forceBoolean(LegacyCancelSubscribeSnakeCase, false)
        forceBoolean(LegacyFirstSubscribe, false)
        forceBoolean(LegacyFirstSubscribeSnakeCase, false)

        SubscriptionLevelFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lcom/lemon/lv/clipmonetize/data/SubscriptionLevel;->ULTRA:Lcom/lemon/lv/clipmonetize/data/SubscriptionLevel;
                return-object v0
            """,
        )
        patched++

        SubscriptionEndTimeFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lcom/lemon/lv/clipmonetize/data/SubscriptionLevel;->ULTRA:Lcom/lemon/lv/clipmonetize/data/SubscriptionLevel;
                if-ne p1, v0, :not_ultra
                const-wide v0, $FAR_FUTURE_UNIX_SECONDS
                return-wide v0
                :not_ultra
                const-wide/16 v0, 0x0
                return-wide v0
            """,
        )
        patched++

        forceLong(SubscriptionNextRenewalFingerprint, "0x0")

        println("[Unlock Ultra Plan] Applied $patched hooks -> Ultra is active locally through 9999-12-31 with no renewal or trial state.")
    }
}
