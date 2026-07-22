package app.chiggi.cryanalyzer.patches.premium

import app.morphe.patcher.Fingerprint

/**
 * jp.firstascent.cryanalyzer.utility.billing.BillingClientWrapper#isPurchased(ProductIdentifier,
 * SubscriptionPlan) -> boolean. Returns true when the product's state in productStateMap is
 * PURCHASED / PURCHASED_AND_ACKNOWLEDGED. The app's premium/subscription gating resolves through this
 * check, so forcing it true makes every subscription query report owned with no purchase. The method
 * name is unique in the class, so this anchors on definingClass + name + returnType (params omitted).
 */
internal object IsPurchasedFingerprint : Fingerprint(
    definingClass = "Ljp/firstascent/cryanalyzer/utility/billing/BillingClientWrapper;",
    name = "isPurchased",
    returnType = "Z",
)

/**
 * jp.firstascent.cryanalyzer.utility.data.UserData#getRestrictionRelease() -> Boolean. This is the
 * app's master "restrictions removed" flag (set from the purchase state via PurchaseInfo). It gates
 * everything: the recording/analysis limit (RecordFragment: `getRestrictionRelease() ||
 * getAnalyzeCountByReward() > 0`), ad display, the free-analysis reminder notification, and the
 * billing UI. Forcing it to TRUE gives unlimited analyses with no ads and no free-count/rewarded-ad
 * requirement. Returns a boxed Boolean (callers use .booleanValue()), so TRUE avoids any NPE.
 */
internal object GetRestrictionReleaseFingerprint : Fingerprint(
    definingClass = "Ljp/firstascent/cryanalyzer/utility/data/UserData;",
    name = "getRestrictionRelease",
    returnType = "Ljava/lang/Boolean;",
)
