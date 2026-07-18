package app.chiggi.hotstar.patches.misc.proxy

import app.morphe.patcher.Fingerprint

/**
 * com.hotstar.persistencestore.impl.data.remote.ProxyStateInterceptor$intercept$2$1 — the coroutine
 * (SuspendLambda) launched from ProxyStateInterceptor.intercept() to process the response. It reads
 * the server's proxy verdict header (X-Hs-SetProxyState: NO_PROXY/RESTRICTED/BLOCKED) and writes it
 * into the persistence store (Ef.a). When the stored state is BLOCKED/RESTRICTED, the app raises the
 * NET_201 "network security error" screen ("Something is interfering with your secure connection").
 *
 * invokeSuspend is the coroutine body. Kotlin's Unit is R8-renamed to Lcom/... (here Pj.e); the
 * class name is a synthetic lambda so this is version-specific and pinned to 26.06.22.3.
 */
internal object ProxyStateVerdictHandlerFingerprint : Fingerprint(
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    definingClass =
        "Lcom/hotstar/persistencestore/impl/data/remote/ProxyStateInterceptor\$intercept\$2\$1;",
)

/**
 * PersistenceStoreImpl's periodic proxy-state refresh coroutines. Because the verdict handler above
 * never stores a state, the store's TTL check (inspectExpiredProxyStates) always sees it as
 * expired/missing and keeps re-launching these refreshments -> constant re-fetch of the enrich/
 * location endpoints -> the app "waits/retries over and over". Neutering these bodies to return
 * immediately stops the periodic re-fetch while leaving the bypass intact (state stays unset =
 * unblocked) and the one-off fetch (done by the interceptor on normal requests) untouched.
 */
internal object EnrichRefreshmentFingerprint : Fingerprint(
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    definingClass =
        "Lcom/hotstar/persistencestore/impl/PersistenceStoreImpl\$launchEnrichServiceRefreshment\$1;",
)

internal object LocationRefreshmentFingerprint : Fingerprint(
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    definingClass =
        "Lcom/hotstar/persistencestore/impl/PersistenceStoreImpl\$launchLocationServiceRefreshment\$1;",
)
