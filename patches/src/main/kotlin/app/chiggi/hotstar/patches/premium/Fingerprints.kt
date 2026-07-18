package app.chiggi.hotstar.patches.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Premium UI unlock fingerprints. Approach credited to Paresh-Maheshwari (GPL-3.0). Protobuf-model
 * class names are stable; the nudge handler anchors on strings. Verified present on 26.06.22.3.
 */

// MyPageData.getIsSubscribed() — protobuf-generated, stable.
internal object IsSubscribedFingerprint : Fingerprint(
    definingClass = "Lcom/hotstar/ui/model/pagedata/MyPageData;",
    name = "getIsSubscribed",
    returnType = "Z",
)

// DownloadInfo.getIsPremium() — protobuf-generated, stable.
internal object IsPremiumFingerprint : Fingerprint(
    definingClass = "Lcom/hotstar/ui/model/feature/download/DownloadInfo;",
    name = "getIsPremium",
    returnType = "Z",
)

// Subscription-nudge handler — anchors on its string constants.
internal object SubscriptionNudgeFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        string("interventionWidget"),
        string("null cannot be cast to non-null type com.hotstar.bff.models.widget.BffWidgetVisibilityIntervention"),
        string("SubscriptionNudgeWidget"),
    ),
)
