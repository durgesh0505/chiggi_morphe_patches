package app.chiggi.zombiecatcher.patches.license

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch

private const val RESOURCE_ROOT = "zombiecatcher"
private object LoggerStubResourceAnchor

// The Play-distributed logger is PairIP protected. Its stripped relocations are restored by the
// encrypted startup program, which cannot run after re-signing. The original logger also supplies
// the game's GNU and LLVM C++ runtimes. Replace it with a four-symbol logger shim and official ARM64
// runtime libraries from NDK r17c and r28c, preserving native ABI dependencies without PairIP.
internal val disableProtectedLoggerPatch = rawResourcePatch {
    execute {
        listOf(
            "lib/arm64-v8a/libst-errorlogger.so" to "libst-errorlogger.so",
            "lib/arm64-v8a/libgnustl_shared.so" to "libgnustl_shared.so",
            "lib/arm64-v8a/libc++_shared.so" to "libc++_shared.so",
        ).forEach { (targetPath, resourceName) ->
            val resourcePath = "$RESOURCE_ROOT/$resourceName"
            val classLoader = LoggerStubResourceAnchor::class.java.classLoader
            val bytes = classLoader.getResourceAsStream(resourcePath)?.use { it.readBytes() }
                ?: throw PatchException("Bundled $resourcePath not found")
            if (bytes.size < 1024 || bytes[0] != 0x7F.toByte() ||
                bytes[1] != 'E'.code.toByte() || bytes[2] != 'L'.code.toByte() ||
                bytes[3] != 'F'.code.toByte()
            ) {
                throw PatchException("Bundled $resourcePath is not a valid ELF file")
            }

            val target = get(targetPath)
            target.parentFile?.mkdirs()
            target.writeBytes(bytes)
        }
    }
}
