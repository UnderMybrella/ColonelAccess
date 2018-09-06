package org.abimon.colonelAccess.handle

import com.sun.jna.Pointer
import org.abimon.colonelAccess.osx.OSXMemoryAccessor
import org.abimon.colonelAccess.osx.SystemB
import org.abimon.colonelAccess.win.WindowsMemoryAccessor
import java.util.*
import kotlin.collections.ArrayList

abstract class MemoryAccessor<E: Any, P: Pointer>(open val pid: Int, open val errorClass: Class<E>, open val pointerClass: Class<P>) {
    abstract val detail: String

    abstract fun readMemory(address: Long, size: Long): Pair<P?, E?>
    abstract fun deallocateOurMemory(pointer: P): E?

    abstract fun getNextRegion(address: Long): Pair<MemoryRegion?, E?>

    open fun getAllRegions(): Array<MemoryRegion> {
        val regions: MutableList<MemoryRegion> = ArrayList()

        var address = 0L

        while (address >= 0) {
            val (region, kret) = getNextRegion(address)

            if (region == null) {
                address = -1
            } else {
                regions.add(region)
                address = region.start + region.size
            }
        }

        return regions.toTypedArray()
    }

    open fun deallocateMemory(pointer: Pointer): E? {
        if (!pointerClass.isInstance(pointer))
            return null

        return deallocateOurMemory(pointerClass.cast(pointer))
    }

    open fun readInt(address: Long): Pair<Int?, E?> {
        val (memory, error) = readMemory(address, 4)
        return memory?.getInt(0) to error
    }

    open fun readInts(vararg addresses: Long): Map<Long, Pair<Int?, E?>> =
            addresses.map { addr -> addr to readInt(addr) }.toMap()

    companion object {
        fun accessorForSystem(pid: Int): MemoryAccessor<*, *> {
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

        fun detailForPID(pid: Int): String {
            val os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH)
            if (os.indexOf("mac") >= 0 || os.indexOf("darwin") >= 0) {
                return SystemB.proc_pidpath(pid).first
            } else if (os.indexOf("win") >= 0) {
                //return WindowsMemoryAccessor(pid)
            } else if (os.indexOf("nux") >= 0) {
                //return LinuxMemoryAccessor(pid)
            }

            TODO("Implement pid details for $os")
        }

        val ourPID: Int by lazy {
            val os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH)
            if (os.indexOf("mac") >= 0 || os.indexOf("darwin") >= 0) {
                return@lazy SystemB.INSTANCE.getpid()
            } else if (os.indexOf("win") >= 0) {
                //return WindowsMemoryAccessor(pid)
            } else if (os.indexOf("nux") >= 0) {
                //return LinuxMemoryAccessor(pid)
            }

            TODO("Implement pid details for $os")
        }
    }
}