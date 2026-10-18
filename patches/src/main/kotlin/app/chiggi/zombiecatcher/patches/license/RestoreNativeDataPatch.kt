package app.chiggi.zombiecatcher.patches.license

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

private const val NATIVE_LIB = "lib/arm64-v8a/libcocos2dcpp.so"
private const val DATA_RESOURCE = "zombiecatcher/libcocos2dcpp-data.bin"

// v1.69.16 file layout: .data section and the PairIP DT_INIT entry in .dynamic.
private const val DATA_OFFSET = 0x15bc580L
private const val DATA_SIZE = 0xa648
private const val INIT_ENTRY_OFFSET = 0x1740000L + 34 * 16
private const val DT_INIT = 12L
private const val PAIRIP_INIT = 0x1600000L

private const val ENCRYPTED_SHA256 = "7e073a69fbf955a776357bc3d8006f736ee095a6a28feab96cc3b13a8f377286"
private const val DECRYPTED_SHA256 = "e647c8534df306fb14abda8e8482572db6cf464b9aca1d1e69dcce9dcfa2e13c"

private object NativeDataResourceAnchor

// PairIP ships .data encrypted and decrypts it from DT_INIT with a VM program whose key Play plants
// in the Keystore. A re-signed build cannot get that key, so the game runs on ciphertext constants
// and renders a black screen. Write the decrypted .data and drop the PairIP DT_INIT entry.
internal val restoreNativeDataPatch = rawResourcePatch {
    execute {
        val lib = get(NATIVE_LIB)
        if (!lib.exists()) throw PatchException("$NATIVE_LIB not found")

        val decrypted = NativeDataResourceAnchor::class.java.classLoader
            .getResourceAsStream(DATA_RESOURCE)?.use { it.readBytes() }
            ?: throw PatchException("Bundled $DATA_RESOURCE not found")
        if (decrypted.size != DATA_SIZE || sha256(decrypted) != DECRYPTED_SHA256) {
            throw PatchException("Bundled $DATA_RESOURCE does not match the expected checksum")
        }

        RandomAccessFile(lib, "rw").use { file ->
            val current = ByteArray(DATA_SIZE)
            file.seek(DATA_OFFSET)
            file.readFully(current)
            when (sha256(current)) {
                DECRYPTED_SHA256 -> Unit
                ENCRYPTED_SHA256 -> {
                    file.seek(DATA_OFFSET)
                    file.write(decrypted)
                }
                else -> throw PatchException("$NATIVE_LIB .data does not match v1.69.16; refusing to patch")
            }

            val entries = ByteArray(32)
            file.seek(INIT_ENTRY_OFFSET)
            file.readFully(entries)
            val dynamic = ByteBuffer.wrap(entries).order(ByteOrder.LITTLE_ENDIAN)
            val tag = dynamic.getLong(0)
            val value = dynamic.getLong(8)
            val nextTag = dynamic.getLong(16)
            if (tag == 0L && value == 0L) return@execute
            if (tag != DT_INIT || value != PAIRIP_INIT || nextTag != 0L) {
                throw PatchException(
                    "$NATIVE_LIB dynamic entry 34 is not the PairIP DT_INIT " +
                        "(tag $tag value 0x${value.toString(16)}); refusing to patch",
                )
            }
            file.seek(INIT_ENTRY_OFFSET)
            file.write(ByteArray(16))
        }
    }
}

private fun sha256(bytes: ByteArray) =
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
