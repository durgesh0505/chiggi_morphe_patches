# CapCut patch plan

## Target

| Field | Value | Verification state |
|---|---|---|
| App | CapCut | Confirmed (`aapt dump badging`) |
| Package | `com.lemon.lvoverseas` | Confirmed |
| Version | `20.0.0` (versionCode `20001600`) | Confirmed |
| ABI | `arm64-v8a` only, minSdk 23, 35 dex | Confirmed |
| Launcher | `com.vega.main.MainActivity` | Confirmed |
| Input APK | `workspace/input/capcut/com.lemon.lvoverseas_s25ultra.apk` (204,918,055 bytes) | File exists |
| Output APK | `workspace/output/capcut/capcut-morphe.apk` | Proposed; not built |
| Patch package | `patches/src/main/kotlin/app/chiggi/capcut/` | Proposed; not created |
| PairIP | None (`unzip -l` shows no `pairip`) | Confirmed |

## References (GPLv3, credit in patch descriptions and README)

| Repo | Target | Use |
|---|---|---|
| `kveld9/kveld-extra-morphe-patches` | CapCut 19.9.0 | Primary source: premium, watermark, export, templates, signature/CrackingInterceptor, geoblock, effects, unsafe modal, billing, telemetry, API v2-boot |
| `riky-dev/morphe-patches` | CapCut 9.0.0 | WireGuard tunnel extension design and effects platform idea only; R8 names are too old to reuse |

## Operator decisions (2026-11-08)

| Decision | Value |
|---|---|
| Approach | Port into this repo, pin to 20.0.0, re-verify every hook |
| Features | Pro + no watermark, premium templates, region/effects bypass, telemetry + billing off |
| Login | Remove the login requirement entirely if possible |
| CrackingInterceptor | Neutralize it safely |
| Region | Must work in India; must not show "No internet connection"; make the app think it is in the USA |
| VPN | India users will optionally run a US VPN; add a patch so CapCut does not see the VPN |
| Extra patches | Operator asked for more suggestions (see candidate list) |

## Static analysis: kveld hook existence in 20.0.0 (2026-11-08)

59 of 63 kveld fingerprints resolve by definingClass + name + signature. Misses: `CutSamePurchaseViewModel.u6` (missing; 20.0.0 has `A6/C6/D6/x6/z6 ()Z`) and `CutSamePurchaseViewModel.w6` (now `w6(ZLorg/json/JSONObject;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function1;)V`). Existence does not prove the same meaning: R8 short names can point at a different method. Semantic verification is in progress.

## Hide-VPN surfaces (verified 2026-11-08)

| Hook | Role | Action |
|---|---|---|
| `Lcom/vega/infrastructure/util/NetworkUtils;->g()Z` | `hasTransport(4)`; only callers are `AccountReport` `is_vpn` telemetry | Force `false` |
| `Lcom/ttnet/org/chromium/net/AndroidNetworkLibrary;->checkIsVpnOn()Z` | Called by native `libsscronet.so` (`is_vpn_on`, `Cronet_NetworkReport_has_vpn`) | Force `false`; device test required (native usage not fully verified) |
| `Lcom/bytedance/common/utility/DeviceUtils;->isWifiProxy(Landroid/content/Context;)Z` | Proxy flag, no Java callers | Force `false` |
| `NetworkChangeNotifierAutoDetect`, `getCurrentVpnNetId` | TTNet socket routing over the VPN | Do not touch: hiding VPN there breaks routing and causes "No internet" |
| `libmetasec_ov.so`, `libenvguard.so` | Native ByteDance security SDK fingerprint | Not bytecode-patchable |
| Server IP reputation | Server sees the VPN exit IP | Not patchable |

## Region / "think it is USA" (investigator report 2026-11-08; signatures re-checked in `index.json`)

No client-side "IN"/India check exists in app code. Region values are only reported to the server; the server decides by IP and echoes its own `store_region` back.

| Hook (smali) | Role | Sent to server | Action |
|---|---|---|---|
| `Lcom/vega/core/utils/FlavorLocale;->b()Ljava/lang/String;` | App region (108 refs): `region` header, `region` param, passport `app_region` | Yes | Return `"US"` |
| `FlavorLocale->c(Landroid/content/Context;)Ljava/lang/String;` | `carrier_region` | Yes | Return `"US"` |
| `FlavorLocale->d(Landroid/content/Context;)Ljava/lang/String;`, `->e(Landroid/content/Context;)Ljava/lang/String;` | Network ISO / SIM ISO | Feeds b/c | Return `"US"` |
| `FlavorLocale->g()Ljava/lang/String;` | `sys_region` | Yes | Return `"US"` |
| `FlavorLocale->k()Ljava/lang/String;` | SIM country upper-case | Feeds b | Return `"US"` |
| `Lcom/bytedance/bpea/tt/entry/api/device/info/CarrierEntry$Companion;` d/e (network ISO), j/k (SIM ISO), f/g (network operator), l/m (SIM operator), h/i (network operator name), n/o (SIM operator name); params `(Landroid/telephony/TelephonyManager;)` or `(Landroid/telephony/TelephonyManager;Lcom/bytedance/bpea/basics/Cert;)`, return `Ljava/lang/String;` | ByteDance TelephonyManager wrapper: `mcc_mnc`, `carrier_region`, device-register `sim_region`/`carrier` | Yes | ISO → `"us"`, operator → `"310260"`, name → `"T-Mobile"` |
| `Lcom/bytedance/bpea/tt/entry/api/device/info/LocaleEntry$Companion;->b(Ljava/util/Locale;)Ljava/lang/String;` | `Locale.getCountry` wrapper (18 callers) | Yes | Return `"US"` |
| `FlavorLocale->h()` (app language) | In-app UI language | Yes | Do not patch; set English in CapCut settings |
| `store_region` (`VeryImportantConfig.y`) | Server verdict echoed back | Echo | Do NOT spoof (RULEBOOK stateful-echo rule) |
| JVM `TimeZone.getDefault()` | `timezone_name`, `tz_offset` | Yes | Do not change (shifts draft/UI times) |

Risks: US headers on an Indian IP is a stronger tamper signal than consistent IN values, so ship the spoof as a toggle and pair it with a US VPN. `LocaleEntry` reaches the GlobalPay/RiskInfo config; a US region against an Indian Play account can show `mob_m10n_sku_unavail_popup_desc_gp`.

## "No internet connection" (investigator report 2026-11-08)

| Source | Patchable | Notes |
|---|---|---|
| Local check `Lcom/vega/infrastructure/util/NetworkUtils;->e()Z` (263 refs) / `->f()Z` | Yes | Already true under a working VPN; force true is safe but fixes nothing in India |
| Home template tab `BaseMainTabViewPagerFragment.java:239,322-334` shows `qbh` when the category request fails OR returns an empty list | No | An empty region-refused server response reads as "No internet" |
| TTNet `CronetIOException` (-105 DNS, -101 reset, -7 timeout) | No | Real transport failures; only a US exit IP fixes them |
| TTNet HttpDNS `AppConfig.isCronetHttpDnsOpen()Z` | Not worth it | Needs TNC `httpdns_addr` fetched from blocked hosts; does nothing vs SNI/IP/geo blocks |

## ⚠️ Native-protected methods (libluster.so)

A protector registers JNI natives from `com.illusion.light.EffectUtil.Ew0PFxIIElcAXVdHWFpVCEM0(int)` in about 42 classes' `<clinit>`. Native methods have no bytecode, so Morphe cannot patch them; removing the native flag would likely make `RegisterNatives` fail at class init (unverified, assumed crash). Confirmed by dexdump: `Lcom/vega/subscribe/SubscribeImpl;` `b`, `f`, `h` are native, but kveld's hook target `isVip()` in that class is not. Reported native (being re-verified): `Lcom/lemon/editor/proxy/SubscribeImpl;->isVip()Z`, `Lcom/vega/subscribe/VipSubscribeManager;->g()Z`, `AdFreeManager.a`, `SubscriptionUserInfo.f`, `MultiRewardAd`, `VipExportChecker`, `VipFeaturePayChecker`, `ExportTrialStrategyChecker`. Rule: hook the Java wrapper or caller, never a native method.

## Login removal (investigator report 2026-11-08)

CapCut already edits and exports logged out (`EditExportController.b()` reads `isLogin()` only for a log line). Do NOT force `isLogin()` true: callers then use `getUserId()` and a server token. Do NOT block the `//login` route globally: `SmartRoute.buildIntent()` returning null NPEs `AccountLogManager.K/Z` and `LoginUtilKt.buildLoginIntent`.

| Hook (smali) | Role | Action |
|---|---|---|
| `Lcom/vega/main/utils/ColdStartLoginHelper;->h()Z` | Full-screen login on cold start | Return false |
| `Lcom/vega/main/utils/ColdStartLoginHelper;->i()Z` | Same check, suppresses home tool guides | Return false |
| `Lcom/vega/export/loginreward/ExportLoginRewardManager;->b()Z` | "Log in for a reward" dialog after export | Return false (continue callback still runs) |
| `Lcom/vega/export/loginreward/ExportLoginRewardManager;->d()Z` | Login banner on export page | Return false |
| `Lcom/vega/subscription/biz/checker/LoginChecker;->e(Lcom/vega/subscription/core/checker/CheckerContext;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;` | "user not login" fail before template checkers | Return `new CheckResult(0, null, null, false)`; only with the premium patch |
| `Lcom/vega/subscription/biz/domain/TemplateBreakDomain;->e(Lcom/vega/container/session/core/ISession;Ljava/lang/String;)Z` | Template locked when `!isLogin()` before `isVip()` | Return false; only with the premium patch |
| `Lcom/lemon/LoginUtilKt;->doLogin(Landroid/app/Activity;Landroid/os/Bundle;ZLkotlin/jvm/functions/Function1;)V` | Single entry for all login wrappers | Optional, `default = false`: invoke callback with `FALSE` and return; server features then become dead buttons; about 20 direct `//login` routes are not covered |

Cannot work logged out (server session required): cloud drafts/space, purchases and paid-draft unlock, TikTok bind/music/publish, Business share, AI and credit features (super-resolution, AI effects, AI video/image templates, AI music, clone tone), favorites, inbox, comments/likes.

## Ads (investigator report 2026-11-08)

Every ad flow checks `BaseAdConfigData.i()` and has its own "no ad" path (export continues, template returns `"free"`).

| Hook (smali) | Action |
|---|---|
| `Lcom/vega/adapi/config/BaseAdConfigData;->i()Z` | Return false (master switch) |
| Overrides `Lcom/vega/adapi/config/SplashAdConfigData;->i()Z`, `TemplateRewardAdConfigData;->i()Z`, `AITemplateRewardAdConfig;->i()Z`, `TemplateRewardDistributeAdConfigData;->i()Z`, `TemplateMultiAdConfig;->i()Z` | Return false (virtual overrides bypass the base hook) |
| `Lcom/vega/business/ad/impl/AdServiceImpl;->h(ZZ)Z` | Return false (splash: cold start, hot start, credit task) |
| Backups: `FreeEditExportRewardAdKMPImpl.b(...)`, `T4RewardAdViewModel.y6(...)`, `FreeCutsameAdViewModel.D6(...)` | Invoke `RewardCallBack.a(new RewardShowResult(true,false,"auto_skip"))` = the app's own fallback |
| Do NOT stub `AdServiceImpl.d(...)` (SDK init) | Launch waits on its callbacks; stubbing can hang splash (unverified) |

Side effect: free credits earned by watching ads disappear.

## Forced update (investigator report 2026-11-08)

| Hook | Action |
|---|---|
| `Lcom/vega/main/update/VersionUpdateService;->a()Z` | Return false (server popup config incl. `force_update`); no Play in-app update and no client min-version check exist |

## WireGuard tunnel (riky) verdict

Skip for v1. Android allows one VPN at a time, and riky's `hasOtherVpn()` refuses to start when the user's system VPN is up, so it adds nothing for users who already run a US VPN. It needs the operator's own US WireGuard `.conf`, a consent dialog, and a foreground notification. The hook target `ScaffoldApplication.onCreate()V` exists in 20.0.0 if revisited later as a `default = false` opt-in.

## India limits (unverified block mechanism)

A client patch cannot change the network path or the source IP. If Indian ISPs block `*.capcutapi.com` (DNS or IP) or ByteDance geofences Indian IPs, only a US VPN or an embedded tunnel fixes it. Operator verification on Indian data without VPN: `nslookup editor-api.capcutapi.com` and `curl -sI https://editor-api.capcutapi.com`.

## kveld hook verdicts on 20.0.0 (investigator report 2026-11-08, remaps re-checked in `index.json`)

| kveld patch | Verdict |
|---|---|
| Unlock Premium (14 hooks) | KEEP all; none is native. `com.vega.subscribe.SubscribeImpl.isVip` is a Java wrapper over native `VipSubscribeManager.g` |
| Remove Watermark | KEEP `Q0`, `F4`, `v4`, `y2`, `EditConfig.k`; DROP `B4` (scene enable flag, not a removal gate); REMAP missing `D4(BenefitResource)` to `C4(Lcom/lemon/lv/clipmonetize/wrapper/BenefitResource;)Z`; never hook the new `D4` (paywall dialog) |
| Unlock Ultra HD Export (`c/d/e/j/q`) | KEEP all |
| Unlock Premium Templates | Names shifted +3. Correct: `x6()Z`→true, `A6()Z`→true, `C6()Z`→false. kveld `A6`/`z6` force-false would LOCK paid templates; DROP `w6`/`z6` |
| Bypass Signature Check | KEEP `AppUtils.a` (official cert `6F:33:FD:…:DB:31`), `JniToast.showApkError`, `injectSignatureBl$1`, `PainterSdkImpl$init$2`, `GodzillaHookedTagManager.a`, `showGlobalSecurityDialog$1`, `pgl t.g/d`; DROP xt `CrackingInterceptor.intercept` (dead code, never instantiated) |
| CrackingInterceptor | `intercept`/`d` are native (not patchable). Verdict persists to SP `crackingInfo` and is echoed only in AppLog analytics header `crack_code` and feedback/export reports; no Java code locks features on it. Suppress the dialog (default on); stubbing persistence `c(I)V` is a separate option `default = false` per RULEBOOK echo rule |
| GeoBlock (5 getters), Effects (3), Unsafe Modal | KEEP; modal URL can be overridden by server settings `unsafe_pkg_modal` |
| Play Billing (7) | KEEP |
| Telemetry | KEEP except missing `PitayaApplogProxy` (skipped safely); EXCLUDE `TicketGuardProvider` from the manifest purge (request signing; kveld lists it as a "No internet" cause) |
| API v2-boot redirect | KEEP as opt-in |

Native gates that Java hooks may not cover (device test required): `com.lemon.editor.proxy.SubscribeImpl.isVip` (native) is read by `VipExportBusinessFunction`, `EditActivity`, `EditTopBarFragment`, `VipEntranceFragment`, xt `SubscribeHostAbility` (photo module); native checkers `VipExportChecker`/`VipFeatureUseChecker`/`VipFeaturePayChecker`/`GoodsAccessChecker` `.e`. Java upsell gates hooked instead: `Lcom/vega/subscription/biz/function/VipExportBusinessFunction;->j3()Z` and `->w0()Z` (true = show VIP upsell) → false. If a Pro-feature export is still blocked on device, next step is the `VipExportBusinessFunction.W3` result path.

## Final patch set for approval (operator choices 2026-11-08)

| # | Patch | Default | Hooks |
|---|---|---|---|
| 1 | Unlock Premium | On | 14 kveld + `VipExportBusinessFunction.j3/w0`→false |
| 2 | Remove Watermark | On | `Q0`,`C4`,`F4`,`v4`→true; `y2`,`EditConfig.k`→false |
| 3 | Unlock Ultra HD Export | On | `TemplateExportUltraHdUtils.c/d/e/j/q`→true |
| 4 | Unlock Premium Templates | On | `x6`,`A6`→true; `C6`→false; `TemplateBreakDomain.e`→false; `LoginChecker.e`→pass |
| 5 | Bypass Signature Check | On | 9 direct hooks plus persist-free CrackingInterceptor dependency; includes the template-feed response interceptor passthrough |
| 6 | Persist-free CrackingInterceptor (`c(I)V` stub) | Off when selected alone; automatically included by Bypass Signature Check | 1 |
| 7 | Bypass Unsafe Package Modal | On | `LynxSchemaConfig.<init>` URL |
| 8 | Bypass GeoBlock | On | 5 |
| 9 | Bypass Effects Region | On | 3 (`windows`, did `0`) |
| 10 | Spoof Region to USA | On (operator choice) | `FlavorLocale` b/c/d/e/g/k, `CarrierEntry$Companion` d–o (12), `LocaleEntry$Companion.b` |
| 11 | Hide VPN | On | `NetworkUtils.g`, `AndroidNetworkLibrary.checkIsVpnOn`, `DeviceUtils.isWifiProxy` |
| 12 | Remove Login Prompts | On | `ColdStartLoginHelper.h/i`, `ExportLoginRewardManager.b/d` |
| 13 | Block All Login Screens | On (operator choice) | `LoginUtilKt.doLogin`→callback FALSE |
| 14 | Remove Ads | On | `BaseAdConfigData.i` + 5 overrides, `AdServiceImpl.h(ZZ)`; reward-callback backups |
| 15 | Disable Forced Update | On | `VersionUpdateService.a` |
| 16 | Block Telemetry | On | kveld set minus `TicketGuardProvider` |
| 17 | Neutralize Play Billing | On | 7 |
| 18 | Redirect API to v2-boot | Off | 4 host rewrites |
| 19 | Change Package Name | Off | manifest rename |
| 20 | Debloat locales / native slimmer | Off | speech `.so` removal kills auto-captions and TTS |
| 21 | Unlock Ultra Plan | On | Depends on Unlock Premium; reports `SubscriptionLevel.ULTRA`, far-future local end times, non-auto purchase state, and no trial/expiry flags |

## Implementation steps (gated on operator approval of this plan)

1. Create branch `feature/capcut`.
2. Add `Constants.kt` with `COMPATIBILITY_CAPCUT` pinned to 20.0.0, `apkFileType = APK`.
3. Add one Kotlin patch file per feature under `app/chiggi/capcut/patches/`.
4. `rm patches/build/libs/patches-*.mpp`, then `./gradlew :patches:build generatePatchesList`.
5. Build `workspace/output/capcut/capcut-morphe.apk` with morphe-cli `--keystore Morphe.keystore`.
6. Verify each patched method with `dexdump -d` and `zipalign -c -p 4`.
7. Operator device test; commit only on explicit approval.

## Current scope override (2026-11-08)

The operator approved Morphe patch-source corrections only and explicitly excluded APK creation. Source corrections are recorded in `claudedocs/CAPCUT_PATCH_SOURCE_FIXES.md`. Steps 4–7 above were not run during the correction pass. Do not build a patch bundle or APK, regenerate `patches-list.json`, sign, align, install, or run device tests without separate operator approval.

The separate Ultra local-plan patch is documented in `claudedocs/CAPCUT_ULTRA_PLAN_PATCH.md`. It changes local tier and expiration state only; server-side Ultra credits, storage, receipts, and account entitlements remain unchanged.

## Security-notice root-cause correction (2026-11-08)

Device feedback proved a security notice survived the first signature-bypass implementation. The main dialog lambda and local JNI toast were already suppressed. The surviving path was the unpatched `com.xt.retouch.applauncher.core.interceptor.CrackingInterceptor.intercept` response interceptor, which reads server `ret` values `-4`, `-5`, `34010421`, and `34010422`. The fix converts that interceptor to a transparent `chain.proceed(request)` passthrough and makes Bypass Signature Check depend on `CrackingInterceptor.c(I)V` persistence suppression. Full evidence is in `claudedocs/CAPCUT_SECURITY_NOTICE_ROOT_CAUSE.md`. No APK was built during this correction.
