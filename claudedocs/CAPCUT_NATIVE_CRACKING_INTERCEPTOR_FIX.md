# CapCut native cracking-interceptor removal

## Device evidence

Fresh installed APK verification proves all prior Java-side security hooks are present, including the business-sign coroutine, runtime crack-guide getter, main dialog lambda, XT response interceptor, and verdict persistence stub. The security notice still appears.

Logcat continues to show requests passing through `com.vega.launcher.network.interceptors.CrackingInterceptor.intercept(Native Method)`. That method is JNI-protected and cannot be edited directly. CapCut registers it in two network initialization paths:

- `NetworkInitHook.a()`
- `NetworkInitTask.run()`

Each creates an interceptor array, stores the native cracking interceptor at index 6, converts the array to an `ArrayList`, then registers every element with Retrofit/TTNet.

## Root cause and fix

The native interceptor can still process server risk responses and launch a raw security modal independently of every Java render helper already suppressed. Remove index 6 from both interceptor lists immediately after `arrayListOf(...)` returns. Preserve all other header, signature, timeout, region, and network interceptors.

## Network-error evidence

The phone also had an active AdGuard local VPN and DNS `198.18.53.53` despite being described as normal internet. Cronet logged `ERR_NAME_NOT_RESOLVED` for v2-boot hosts. Force-stopping AdGuard removes the VPN, but the app was updated rather than clean-uninstalled (`firstInstallTime` remained unchanged), so TTNet/Cronet caches and app state persist. Clearing app data is destructive and requires separate approval.

`Redirect API to v2 Boot` should remain opt-in for blocked regions, not default-on for U.S. users. U.S. users should leave it unselected unless the default host is demonstrably blocked.

## Completion record

Implemented on 2026-11-09. Both exact network-init descriptors were verified. In each method, `arrayListOf(...)` returns the interceptor list in `v1`; the injected code removes element index 6 before iteration, preserving the other eight interceptors. The injected sequence compiled directly through Morphe InlineSmaliCompiler to `CONST_4` and `INVOKE_VIRTUAL`. Six source assertions and `git diff --check` passed. `Redirect API to v2 Boot` was restored to default-off. No APK or patch bundle was built.
