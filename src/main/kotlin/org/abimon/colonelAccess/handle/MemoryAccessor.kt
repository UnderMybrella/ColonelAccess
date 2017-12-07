package org.abimon.colonelAccess.handle

import com.sun.jna.Pointer
import org.abimon.colonelAccess.osx.OSXMemoryAccessor
import org.abimon.colonelAccess.win.WindowsMemoryAccessor
import java.util.*



abstract class MemoryAccessor<out E>(pid: Int) {
    abstract fun readMemory(address: Long, size: Int): Pair<Pointer?, E?>

    fun readInt(address: Long): Pair<Int?, E?> {
        val (memory, error) = readMemory(address, 4)
        if (memory != null)
            return memory.getInt(0) to error
        return memory to error
    }

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