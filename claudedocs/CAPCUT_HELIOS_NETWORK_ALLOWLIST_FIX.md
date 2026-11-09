# CapCut Helios functional-host allowlist fix

## Device proof

With AdGuard VPN off and Private DNS off, the phone resolves CapCut hosts normally, but Templates still shows “Network error.” Live CapCut logcat repeatedly prints `Helios:Network-White: Block request feed-api.capcutapi.com` exactly when the template page refreshes.

DEX disassembly proves this is an enforcement path, not only logging. `NetworkPipeline.b(...)` calls `INetworkApiService.dropRequest(event, 410, "Only domain names on the allow-list can be requested.")` when `ValidDomainManager.d(host, cache, allowSet)` returns false, then marks the result as intercepted.

## Root cause

The app's runtime Helios allow-list does not contain required functional CapCut hosts in this build/config state. Requests are dropped before normal network transport, so the UI receives no category data and renders its generic network-error state.

## Source fix

Add a separate default-on patch that prepends a narrow suffix allow-list to `ValidDomainManager.d(String, Map, Set)`. Return true only for these functional families, then preserve the original logic for every other domain:

- `capcutapi.com`
- `ibyteimg.com`
- `tiktokcdn-us.com`

This restores feed/config/resource traffic without globally disabling Helios or fabricating API responses.

## Validation

Verify the exact method descriptor, compile the injected suffix checks through Morphe InlineSmaliCompiler, and keep the patch limited to the three domain families. No APK build in this task.

## Completion record

Implemented on 2026-11-09 as `CapcutAllowFunctionalHostsPatch.kt`. The exact `ValidDomainManager.d(String, Map, Set):boolean` descriptor was verified. The 14-instruction suffix guard compiled directly through Morphe InlineSmaliCompiler, eight source assertions passed, and `git diff --check` passed. No APK or patch bundle was built.
