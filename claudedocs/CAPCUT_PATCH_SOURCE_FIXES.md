# CapCut Morphe patch source corrections

## Scope

Source-only corrections for CapCut 20.0.0 Morphe patches. Do not build, sign, align, install, regenerate, or validate an APK.

## Approved corrections

| Status | File | Correction | Proof target |
|---|---|---|---|
| Complete | `CapcutUnlockPremiumPatch.kt` | Apply the declared `VipExportBusinessFunction.j3()` and `w0()` hooks as `false`. | Both fingerprints are referenced by `execute`; existing 14 VIP hooks remain unchanged. |
| Complete | `CapcutBlockTelemetryPatch.kt` | Stop broad stubbing of functional `CepEngineManager` methods; hook only its event dispatcher. Include full method descriptors in dynamic deduplication keys. Preserve device-registration, push, and widget manifest components. | `init`, `configEngine`, registration, and rule methods are not selected; overloads do not collide. |
| Complete | `CapcutUnlockPremiumTemplatesPatch.kt`, `CapcutLoginPatch.kt` | Move `LoginChecker.e` into the premium-template patch and make that patch depend on `Unlock Premium`. | Premium-template behavior is self-contained and selectable safely. |
| Complete | `CapcutAdsPatch.kt` | Add the three verified reward-ad completion fallbacks using `RewardShowResult(true, false, "auto_skip")`. | Export, T4, and CutSame reward entry points invoke their callback and return without loading an ad. |
| Complete | `CapcutChangePackageNamePatch.kt` | Rewrite all verified package-bound manifest identifiers, not only provider authorities. | Instrumentation targets, custom permissions, actions, schemes, hosts, and provider authorities follow the selected package. |
| Complete | Documentation | Update this file, `Table_of_Contents.md`, `Talk.md`, and `learn.md` with exact changes and unresolved runtime limits. | Handoff identifies no APK build or runtime validation. |

## Constraints and risks

The target remains pinned to package `com.lemon.lvoverseas`, version `20.0.0`. No native method is modified. Reward callbacks use verified 20.0.0 method descriptors and the existing three-argument `RewardShowResult` constructor. Source review can prove hook ownership and injected instructions, but it cannot prove server entitlements, native protection behavior, or Android runtime behavior.

## Validation allowed in this task

Read source, inspect diffs, run text/static consistency checks, and verify exact descriptors against the existing input APK without producing a new artifact. Do not run Gradle tasks that build patch bundles or APKs.

## Completion record

Completed on 2026-11-08. `git diff --check` passed for tracked changes, the direct trailing-whitespace scan covered the untracked CapCut source tree, and 28 source assertions passed. No Gradle, Morphe CLI, APK signing, APK alignment, ADB, or installation command was run. The generated `patches-list.json` was not regenerated because that requires building the patch bundle; its descriptions and dependency metadata remain stale until the user separately approves a source-build step.
