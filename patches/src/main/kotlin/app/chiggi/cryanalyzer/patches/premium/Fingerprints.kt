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
