package org.abimon.colonelAccess.handle

import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Psapi
import org.abimon.colonelAccess.osx.OSXMemoryAccessor
import org.abimon.colonelAccess.osx.SystemB
import org.abimon.colonelAccess.win.Colonel32
import org.abimon.colonelAccess.win.WindowsMemoryAccessor
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

abstract class MemoryAccessor<E : Any, P : Pointer>(open val pid: Int, open val errorClass: Class<E>, open val pointerClass: Class<P>) {
    abstract val detail: String

    abstract fun readMemory(address: Long, size: Long): Triple<P?, E?, Long?>
    abstract fun writeMemory(address: Long, data: Pointer, size: Long): Pair<E?, Long?>
    abstract fun deallocateOurMemory(pointer: P): E?

    abstract fun getNextRegion(address: Long): Pair<MemoryRegion?, E?>

//    abstract fun remapMemory(address: Long, size: Long): Pair<P?, E?>

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
        val int = memory?.getInt(0)

        if (memory != null)
            deallocateOurMemory(memory)

        return int to error
    }

    open fun readInts(vararg addresses: Long): Map<Long, Pair<Int?, E?>> =
            addresses.map { addr -> addr to readInt(addr) }.toMap()

    //Override if possible, this is likely slow
    open fun writeMemory(address: Long, data: ByteBuffer): Pair<E?, Long?> {
        val mem = Memory(data.remaining().toLong())

        for (i in 0L until data.remaining())
            mem.setByte(i, data.get())

        return writeMemory(address, mem, mem.size())
    }

    open fun writeMemory(address: Long, data: ByteArray): Pair<E?, Long?> {
        val mem = Memory(data.size.toLong())

        mem.write(0, data, 0, data.size)

        return writeMemory(address, mem, mem.size())
    }

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
                val handle = Colonel32.INSTANCE.OpenProcess(Kernel32.PROCESS_QUERY_LIMITED_INFORMATION, true, pid)
                val processNameArray = CharArray(1024)
                val size = Psapi.INSTANCE.GetModuleFileNameExW(handle, null, processNameArray, processNameArray.size)
                return processNameArray.copyOfRange(0, size).joinToString("")
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
                return@lazy Kernel32.INSTANCE.GetCurrentProcessId()
            } else if (os.indexOf("nux") >= 0) {
                //return LinuxMemoryAccessor(pid)
            }

            TODO("Implement pid details for $os")
        }
    }
}