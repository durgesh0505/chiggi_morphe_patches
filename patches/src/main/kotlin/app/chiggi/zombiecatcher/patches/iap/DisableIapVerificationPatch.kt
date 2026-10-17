package app.chiggi.zombiecatcher.patches.iap

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val NATIVE_LIB = "lib/arm64-v8a/libcocos2dcpp.so"
private const val VERIFICATION_GETTER = "_ZN16FirebaseRCHelper23isIAPVerificationEnableEv"

private const val LDRB_W0_X0_MASK = 0xFFC003FF.toInt()
private const val LDRB_W0_X0 = 0x39400000
private const val MOV_W0_ZERO = 0x52800000
private const val RET = 0xD65F03C0.toInt()

// Forces verifyIAP() onto its offline path, which grants via updateResult(..., true).
internal val disableIapVerificationPatch = rawResourcePatch {
    execute {
        val lib = get(NATIVE_LIB)
        if (!lib.exists()) throw PatchException("$NATIVE_LIB not found")

        val offset = findFunctionFileOffset(lib.readBytes(), VERIFICATION_GETTER)

        RandomAccessFile(lib, "rw").use { file ->
            val code = ByteArray(8)
            file.seek(offset)
            file.readFully(code)
            val words = ByteBuffer.wrap(code).order(ByteOrder.LITTLE_ENDIAN)
            val first = words.getInt(0)
            val second = words.getInt(4)

            if (first == MOV_W0_ZERO && second == RET) return@execute
            if ((first and LDRB_W0_X0_MASK) != LDRB_W0_X0 || second != RET) {
                throw PatchException(
                    "$VERIFICATION_GETTER is not a plain field getter " +
                        "(0x%08x 0x%08x); refusing to patch".format(first, second),
                )
            }

            words.putInt(0, MOV_W0_ZERO)
            words.putInt(4, RET)
            file.seek(offset)
            file.write(code)
        }
    }
}

private fun findFunctionFileOffset(elf: ByteArray, symbol: String): Long {
    val buf = ByteBuffer.wrap(elf).order(ByteOrder.LITTLE_ENDIAN)
    if (buf.getInt(0) != 0x464C457F || elf[4].toInt() != 2 || elf[5].toInt() != 1 ||
        buf.getShort(0x12).toInt() != 183
    ) {
        throw PatchException("$NATIVE_LIB is not a little-endian ELF64 AArch64 library")
    }

    val shOff = buf.getLong(0x28).toInt()
    val shEntSize = buf.getShort(0x3A).toInt() and 0xFFFF
    val shNum = buf.getShort(0x3C).toInt() and 0xFFFF
    fun section(index: Int) = shOff + index * shEntSize

    val dynsym = (0 until shNum).map(::section).firstOrNull { buf.getInt(it + 4) == 11 }
        ?: throw PatchException("$NATIVE_LIB has no .dynsym")
    val symOff = buf.getLong(dynsym + 0x18).toInt()
    val symSize = buf.getLong(dynsym + 0x20).toInt()
    val symEnt = buf.getLong(dynsym + 0x38).toInt()
    val strOff = buf.getLong(section(buf.getInt(dynsym + 0x28)) + 0x18).toInt()

    val wanted = symbol.toByteArray(Charsets.US_ASCII)
    var vaddr = -1L
    for (entry in symOff until symOff + symSize step symEnt) {
        val name = strOff + buf.getInt(entry)
        if (name + wanted.size < elf.size && elf[name + wanted.size].toInt() == 0 &&
            wanted.indices.all { elf[name + it] == wanted[it] }
        ) {
            vaddr = buf.getLong(entry + 8)
            break
        }
    }
    if (vaddr <= 0) throw PatchException("$symbol not exported by $NATIVE_LIB")

    val phOff = buf.getLong(0x20).toInt()
    val phEntSize = buf.getShort(0x36).toInt() and 0xFFFF
    val phNum = buf.getShort(0x38).toInt() and 0xFFFF
    for (i in 0 until phNum) {
        val ph = phOff + i * phEntSize
        if (buf.getInt(ph) != 1) continue
        val pOffset = buf.getLong(ph + 0x08)
        val pVaddr = buf.getLong(ph + 0x10)
        val pFileSize = buf.getLong(ph + 0x20)
        if (vaddr >= pVaddr && vaddr + 8 <= pVaddr + pFileSize) return vaddr - pVaddr + pOffset
    }
    throw PatchException("$symbol address 0x${vaddr.toString(16)} is outside every PT_LOAD segment")
}
