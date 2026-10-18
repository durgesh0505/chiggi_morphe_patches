/*
 * Minimal ABI stub for Zombie Catchers' PairIP-protected libst-errorlogger.so.
 *
 * The original library exports a complete static libc++ runtime. After re-signing, PairIP does not
 * restore its stripped PLT relocations, so libst-errorlogger's operator new jumps to address
 * 0x3f770 and crashes while libcocos2dcpp.so runs global constructors. This stub intentionally
 * exports only the four symbols libcocos2dcpp.so imports from the logger.
 */

typedef int jint;
typedef void JavaVM;

#define EXPORT __attribute__((visibility("default")))
#define SYMBOL(name) __asm__(name)

static void noop_virtual(void) {
}

EXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    (void)vm;
    (void)reserved;
    return 0x00010006;
}

EXPORT void enable_crash_logs(void) SYMBOL("_ZN13STErrorLogger15EnableCrashLogsEv");
EXPORT void enable_crash_logs(void) {
}

EXPORT void send_crash_logs(void) SYMBOL("_ZN13STErrorLogger13SendCrashLogsEv");
EXPORT void send_crash_logs(void) {
}

/* std::shared_ptr<IErrorLogger>: stored pointer + control-block pointer. */
EXPORT __attribute__((aligned(8))) unsigned char error_logger[16]
    SYMBOL("_ZN13STErrorLogger11errorLoggerE") = {0};

/* Original vtable symbol is 104 bytes: two ABI header pointers plus eleven method slots. */
EXPORT __attribute__((aligned(8))) void *crashlytics_logger_vtable[13]
    SYMBOL("_ZTV17CrashlyticsLogger") = {
        0,
        0,
        noop_virtual,
        noop_virtual,
        noop_virtual,
        noop_virtual,
        noop_virtual,
        noop_virtual,
        noop_virtual,
        noop_virtual,
        noop_virtual,
        noop_virtual,
        noop_virtual,
    };
