# CapCut runtime security-modal fix

## Device evidence

The installed CapCut APK was pulled from Samsung SM-S938U1 and inspected directly. Existing hooks are present:

- `CrackingInterceptor$showGlobalSecurityDialog$1.invoke()` returns `Unit` immediately.
- XT `CrackingInterceptor.intercept(...)` is a transparent passthrough.
- `CrackingInterceptor.c(I)V` returns immediately.
- The unsafe-modal constructor default is overwritten with the template portal.

Despite those hooks, the device displays the security notice inside `com.vega.main.TransLynxActivity`. Logcat repeatedly shows the native main `CrackingInterceptor.intercept` path.

## Root cause

The constructor rewrite only changes the default `LynxSchemaConfig.crackGuideDialog` value. Runtime settings can replace that field after construction. The active SPI path is:

`ICapCutLynxSchemeCapCut.t0()` → `LynxSchemeConfigCapCutImpl.t0()` → `s1().J().a()` → runtime `crackGuideDialog` URL.

The installed `LynxSchemeConfigCapCutImpl.t0()` remains unpatched, so a server/settings override restores the security modal URL and opens it in `TransLynxActivity`.

## Source fix

Keep the constructor fallback rewrite and additionally force `LynxSchemeConfigCapCutImpl.t0()` to return the template portal URL directly. This bypasses both default and runtime-overridden crack-guide schemas without modifying unrelated Lynx routes.

## Limits

This prevents rendering the blocking security modal. It does not alter native cracking detection or make server-rejected template data valid. Template request errors require separate response-path evidence.

## Completion record

Implemented on 2026-11-08. The exact CapCut 20.0.0 `LynxSchemeConfigCapCutImpl.t0()Ljava/lang/String;` descriptor was verified. The injected `const-string` plus `return-object` sequence compiled directly through Morphe `InlineSmaliCompiler` to `CONST_STRING` and `RETURN_OBJECT`. Five source assertions and `git diff --check` passed. No APK or patch bundle was built.
