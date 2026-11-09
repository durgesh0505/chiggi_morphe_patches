# CapCut business-sign security dialog fix

## Device proof

On the freshly repatched Samsung SM-S938U1, templates load successfully behind the remaining modal, proving the feed redirect works. The displayed modal exactly matches `com.vega.ui.dialog.BusinessSignDialog` resources and layout.

Installed bytecode proves all previous cracking hooks are present, including the runtime `LynxSchemeConfigCapCutImpl.t0()` override. The remaining dialog is created independently by:

`InitManagerEx.e(Activity, boolean)` → `InitManagerEx$tryShowBusinessSignDialog$1.invokeSuspend(...)` → `BusinessSignDialog` → popup key `creator_sign_alert`.

The launch condition uses server/account `BusinessAccessEntity` sign state. The lambda waits 500 ms and constructs the blocking dialog. Its button opens `ICapCutLynxSchemeCapCut.n()`, but the visible modal itself is native Android UI and does not use the previously patched crack-guide URL.

## Source fix

Add the exact coroutine lambda fingerprint to `Bypass Signature Check` and return `kotlin.Unit` immediately from `invokeSuspend(Object)`. This suppresses only the `creator_sign_alert` business-sign security modal while retaining unrelated startup and popup behavior.

## Validation

Verify the exact CapCut 20.0.0 class/method descriptor, compile the injected Unit-return sequence through Morphe InlineSmaliCompiler, and inspect source assertions. No APK build in this task.

## Completion record

Implemented on 2026-11-08. The exact `invokeSuspend(Object):Object` descriptor was verified, the injected sequence compiled to `SGET_OBJECT` and `RETURN_OBJECT`, four source assertions passed, and `git diff --check` passed. No APK or patch bundle was built.
