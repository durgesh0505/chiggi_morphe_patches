# CapCut Ultra local-plan patch

## Goal

Add a separate `Unlock Ultra Plan` Morphe patch that depends on `Unlock Premium`, reports the highest local subscription tier, `ULTRA`, and treats the local membership as non-expiring.

## Proven model

CapCut 20.0.0 defines `SubscriptionLevel` values `FREE`, `STANDARD`, `VIP`, and `ULTRA`; the serialized Ultra value is `"ultra"`. `APIModelsExtKt.getSubscriptionLevel(UserDetailInfo)` derives the tier from `UserDetailInfo.getCurVipLevel()`. The membership card derives displayed expiration from `APIModelsExtKt.getSubscriptionEndTime(...)`, and timestamps are Unix seconds multiplied by 1000 before formatting.

## Source changes

Implementation file: `patches/src/main/kotlin/app/chiggi/capcut/patches/CapcutUnlockUltraPlanPatch.kt`. The patch is default-on and depends on `capcutUnlockPremiumPatch`, preserving the existing premium boolean gates while keeping tier/expiry spoofing independently maintainable.

| Status | Hook | Return | Purpose |
|---|---|---|---|
| Complete | `UserDetailInfo.getCurVipLevel()` | `"ultra"` | Make direct tier reads identify Ultra. |
| Complete | `APIModelsExtKt.getSubscriptionLevel(UserDetailInfo)` | `SubscriptionLevel.ULTRA` | Make the central tier resolver identify Ultra. |
| Complete | `APIModelsExtKt.getSubscriptionEndTime(UserDetailInfo, SubscriptionLevel)` | `253402300799` for Ultra; `0` for other tiers | Show only Ultra as active through `9999-12-31`. |
| Complete | `UserDetailInfo.getEndTime()`, `getVipRealEnd()`, `getSvipEndTime()`, `getBenefitEndAt()`, `getProtectEndTime()` | `253402300799` | Prevent direct local expiry checks. |
| Complete | `UserDetailInfo.getNextRenewalTime()` | `0` | Represent a non-renewing lifetime local plan. |
| Complete | `UserDetailInfo.getSubscribeType()` | `"un-auto"` | Represent purchased/non-auto-renewing membership. |
| Complete | `UserDetailInfo.isCancelSubscribe()` | `false` | Prevent cancelled-state UI. |
| Complete | `UserDetailInfo.getDidHadVip()` | `true` | Preserve purchased-member history behavior. |
| Complete | `UserDetailInfo.isFreeTrial()`, `getFreeTrial()`, `isPippitFreeTrial()`, `getProtectExpiring()`, `isIapUncancelledExpiredSubscribe()` | `false` | Prevent trial, expiration-warning, and expired-IAP states. |

## Limits

This is a local client-state spoof. It cannot create server-side Ultra ownership, cloud storage allocation, AI credits, account receipts, subscription management records, or protected server responses. Those features can still reject the account even when local UI and feature gates report Ultra.

## Validation scope

Verify exact CapCut 20.0.0 method descriptors and source injections only. Do not create an APK. Do not build a patch bundle unless separately approved.

## Completion record

Implemented on 2026-11-08 as `CapcutUnlockUltraPlanPatch.kt`. All 27 target descriptors were found in CapCut 20.0.0, 12 source assertions passed, `git diff --check` passed, and the source has no trailing whitespace. No Gradle, patch-bundle, APK, signing, alignment, installation, or ADB command ran.
