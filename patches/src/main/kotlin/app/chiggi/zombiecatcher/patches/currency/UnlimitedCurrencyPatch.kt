package app.chiggi.zombiecatcher.patches.currency

import app.chiggi.zombiecatcher.patches.iap.findFunctionFileOffset
import app.chiggi.zombiecatcher.patches.shared.Constants.COMPATIBILITY_ZOMBIE_CATCHERS
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val NATIVE_LIB = "lib/arm64-v8a/libcocos2dcpp.so"
private val CURRENCY_GETTERS = listOf(
    "_ZN8GameData17currentCashAmountEv",
    "_ZN8GameData28currentPremiumCurrencyAmountEv",
)

// Original prologue: stp x29, x30, [sp, #-0x20]! ; str x19, [sp, #0x10]
private const val STP_PROLOGUE = 0xA9BE7BFD.toInt()
private const val STR_X19 = 0xF9000BF3.toInt()

// movn w0, #0xb000, lsl #16 → 0x4fffffff (1,342,177,279). Both currencies are 32-bit int;
// this leaves headroom so the game adding a reward to the value cannot overflow negative.
private const val MOVN_W0_AMOUNT = 0x12B60000
private const val RET = 0xD65F03C0.toInt()

private val unlimitedCurrencyNativePatch = rawResourcePatch {
    execute {
        val lib = get(NATIVE_LIB)
        if (!lib.exists()) throw PatchException("$NATIVE_LIB not found")
        val elf = lib.readBytes()

        RandomAccessFile(lib, "rw").use { file ->
            CURRENCY_GETTERS.forEach { symbol ->
                val offset = findFunctionFileOffset(elf, symbol)
                val code = ByteArray(8)
                file.seek(offset)
                file.readFully(code)
                val words = ByteBuffer.wrap(code).order(ByteOrder.LITTLE_ENDIAN)
                val first = words.getInt(0)
                val second = words.getInt(4)

                if (first == MOVN_W0_AMOUNT && second == RET) return@forEach
                if (first != STP_PROLOGUE || second != STR_X19) {
                    throw PatchException(
                        "$symbol has an unexpected prologue " +
                            "(0x%08x 0x%08x); refusing to patch".format(first, second),
                    )
                }

                words.putInt(0, MOVN_W0_AMOUNT)
                words.putInt(4, RET)
                file.seek(offset)
                file.write(code)
            }
        }
    }
}

@Suppress("unused")
val unlimitedCurrencyPatch = bytecodePatch(
    name = "Unlimited coins and plutonium",
    description = "Makes the game always read 1,342,177,279 coins and plutonium. Both are " +
        "32-bit values, so this is near the maximum while leaving room for rewards to be added " +
        "without overflowing. Spending never lowers the balance. Use a throwaway account.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_ZOMBIE_CATCHERS)
    dependsOn(unlimitedCurrencyNativePatch)

    execute { }
}
