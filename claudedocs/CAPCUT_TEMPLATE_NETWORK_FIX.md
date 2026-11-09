# CapCut delayed taskbar and template network fix

## Observed behavior

The in-app template task/tab bar remains in loading state for minutes, then appears, while the template surface displays “No internet connection.”

## Proven root cause

The selected CapCut set contains 17 patches, exactly matching the 17 default-enabled patches in Chiggi Patches 1.27.0. `Redirect API to v2 Boot` is the relevant eighteenth patch and is disabled by default.

`BaseMainTabViewPagerFragment` keeps the template page in `loading` while awaiting the category list. A request exception switches the state view to `error` and shows the network message; an empty list also selects `error`. Only a non-empty category list hides the state view and completes the tab/taskbar setup.

The category request path is:

`NFeedCategoryRemoteDataSource` → `FeedApiService.fetchCategoryList(...)` → `FeedApiServiceFactory` → `APIHost.community` → `feed-api.capcutapi.com`.

The existing redirect patch rewrites that host to `feed-api-v2-boot.capcutapi.com`, plus the editor, commerce, and passport API hosts. Because the redirect patch was not selected, the template category request remained on the blocked/default host until timeout or delayed fallback.

## Source fix

Make `Redirect API to v2 Boot` default-on and update its description to name template category/tab loading. Do not alter retry timing, fake connectivity, return synthetic categories, or bypass TTNet routing. Real server, DNS, VPN-exit, or account failures remain possible.

## Validation scope

Source and metadata checks only. Do not build an APK. A later release/build must show 18 default CapCut patches and include the four verified host rewrites.

## Completion record

Implemented on 2026-11-08 by making `Redirect API to v2 Boot` default-on and naming template-category/tab loading in its metadata. Seven source assertions passed. DEX disassembly confirmed executable `const-string` targets for all four hosts: editor 20, feed 35, commerce 8, and passport 4. No patch bundle or APK was built.
