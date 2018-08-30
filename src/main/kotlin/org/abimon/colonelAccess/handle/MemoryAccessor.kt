package org.abimon.colonelAccess.handle

import com.sun.jna.Pointer
import org.abimon.colonelAccess.osx.OSXMemoryAccessor
import org.abimon.colonelAccess.win.WindowsMemoryAccessor
import java.util.*

abstract class MemoryAccessor<out E>(pid: Int) {
    abstract fun readMemory(address: Long, size: Int): Pair<Pointer?, E?>

    fun readInt(address: Long): Pair<Int?, E?> {
        val (memory, error) = readMemory(address, 4)
        return memory?.getInt(0) to error
    }

    fun readInts(vararg addresses: Long): Map<Long, Pair<Int?, E?>> =
            addresses.map { addr -> addr to readInt(addr) }.toMap()

    companion object {
        fun accessorForSystem(pid: Int): MemoryAccessor<*> {
            val os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH)
            if (os.indexOf("mac") >= 0 || os.indexOf("darwin") >= 0) {
                return OSXMemoryAccessor(pid)
            } else if (os.indexOf("win") >= 0) {
                return WindowsMemoryAccessor(pid)
            } else if (os.indexOf("nux") >= 0) {
                //return LinuxMemoryAccessor(pid)
            }

            TODO("Implement memory accessors for $os")
        }
    }
}