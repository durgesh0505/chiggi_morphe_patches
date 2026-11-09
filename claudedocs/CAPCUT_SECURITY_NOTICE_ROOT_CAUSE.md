# CapCut security notice root cause and source fix

## Symptom

CapCut displays: “The current version of the app is not secure. Download the latest version in the official app store.”

## Proven root cause

The existing `Bypass Signature Check` patch neutralizes the main `com.vega.launcher.network.interceptors.CrackingInterceptor` dialog lambda, but it does not neutralize CapCut's second cracking interceptor:

`Lcom/xt/retouch/applauncher/core/interceptor/CrackingInterceptor;->intercept(Lcom/bytedance/retrofit2/intercept/Interceptor$Chain;)Lcom/bytedance/retrofit2/SsResponse;`

That Java interceptor proceeds with the request, parses a string response body, reads the `ret` field, and treats `-4`, `-5`, `34010421`, and `34010422` as cracking verdicts. The current patched APK retains that original method body. The maintained upstream CapCut patch documents that this second interceptor relaunches the security notice for those server verdicts and replaces it with a transparent request/response passthrough.

A second persistence path remains active: `Lcom/vega/launcher/network/interceptors/CrackingInterceptor;->c(I)V` writes `isCracking=true`, `updateTime`, and `crackCode` to the `crackingInfo` shared preferences. The current output retains that method because the separate persistence patch is optional and disabled by default. Persisting the verdict allows later requests and reports to continue carrying the cracked state.

The local `JniToast.showApkError` path is not the observed surviving path: it is already patched to return immediately, and its packaged English text is “Abnormal download source, please download the official version from app store,” not the reported modal text. The main global Lynx dialog lambda is also already patched to return `Unit` immediately.

## Source fix

1. Make `Bypass Signature Check` depend on `Do Not Persist Cracking Verdict`, so the verdict cannot be stored.
2. Add the exact second-interceptor fingerprint and replace its body with `chain.request()` followed by `chain.proceed(request)` and direct response return.
3. Keep the main dialog, toast, signature hash, Godzilla, Painter, and PGL hooks unchanged.
4. Update descriptions and source documentation.

## Scope and validation

Source changes only. Do not build, sign, align, install, regenerate, or test an APK. Validate exact fingerprints against the existing CapCut 20.0.0 input, inspect source diffs, and run static assertions only. Runtime confirmation still requires a later user-approved rebuild and clean-data device test.
