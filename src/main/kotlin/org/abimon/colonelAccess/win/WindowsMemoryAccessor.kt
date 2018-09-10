package org.abimon.colonelAccess.win

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.*
import com.sun.jna.ptr.IntByReference
import org.abimon.colonelAccess.handle.MemoryAccessor
import org.abimon.colonelAccess.handle.MemoryRegion

open class WindowsMemoryAccessor(pid: Int) : MemoryAccessor<Int, WinMemory>(pid, Int::class.java, WinMemory::class.java) {
    protected val process: WinNT.HANDLE = Colonel32.INSTANCE.OpenProcess(Kernel32.PROCESS_VM_READ or Kernel32.PROCESS_VM_WRITE or Kernel32.PROCESS_QUERY_INFORMATION, true, pid)
            ?: throw IllegalAccessException("Error: No access granted to gain read permissions for $pid")
    protected val baseAddress: Long

    final override val detail: String = run {
        val processNameArray = CharArray(1024)
        val size = Psapi.INSTANCE.GetModuleFileNameExW(process, null, processNameArray, processNameArray.size)
        return@run processNameArray.copyOfRange(0, size).joinToString("")
    }

    override fun readMemory(address: Long, size: Long): Triple<WinMemory?, Int?, Long?> {
        val read = IntByReference()
        val output = WinMemory(size)

        if (Colonel32.INSTANCE.ReadProcessMemory(process, Pointer(baseAddress + address), output, size.toInt(), read))
            return Triple(output, null, read.value.toLong())
        else if (Colonel32.INSTANCE.GetLastError() == 299) { //ERROR_PARTIAL_COPY
            output.readSize = read.value.toLong()
            return Triple(output, null, read.value.toLong())
        }

        output.dispose()
        return Triple(null, Colonel32.INSTANCE.GetLastError(), 0L)
    }

    override fun deallocateOurMemory(pointer: WinMemory): Int? {
        pointer.dispose()

        return null
    }

    override fun getNextRegion(address: Long): Pair<MemoryRegion?, Int?> {
        val info = WinNT.MEMORY_BASIC_INFORMATION()
        val infoSize = BaseTSD.SIZE_T(info.size().toLong())

        if(Colonel32.INSTANCE.VirtualQueryEx(process, Pointer(address), info, infoSize).toLong() == infoSize.toLong()) {
            val regionNameArray = CharArray(1024)
            val regionNameSize = Psapi.INSTANCE.GetModuleFileNameExW(process, info.allocationBase?.let(WinNT::HANDLE), regionNameArray, regionNameArray.size)
            val regionName = regionNameArray.copyOfRange(0, regionNameSize).joinToString("")

            return MemoryRegion(Pointer.nativeValue(info.baseAddress), info.regionSize.toLong(), Colonel32.allocationProtectToUnix(info.allocationProtect.toInt()), regionName) to null
        } else {
            return null to Colonel32.INSTANCE.GetLastError()
        }
    }

    override fun getAllRegions(): Array<MemoryRegion> {
        val info = WinNT.MEMORY_BASIC_INFORMATION()

        var p = Pointer(0)

        val infoSize = BaseTSD.SIZE_T(info.size().toLong())

        val regions: MutableList<MemoryRegion> = ArrayList()

        while (Colonel32.INSTANCE.VirtualQueryEx(process, p, info, infoSize).toLong() == infoSize.toLong()) {
            val regionNameArray = CharArray(1024)
            val regionNameSize = Psapi.INSTANCE.GetModuleFileNameExW(process, info.allocationBase?.let(WinNT::HANDLE), regionNameArray, regionNameArray.size)
            val regionName = regionNameArray.copyOfRange(0,regionNameSize).joinToString("")

            regions.add(MemoryRegion(Pointer.nativeValue(info.baseAddress), info.regionSize.toLong(), Colonel32.allocationProtectToUnix(info.allocationProtect.toInt()), regionName))

            p = p.share(info.regionSize.toLong())
        }

        return regions.toTypedArray()
    }

    init {
        val lphModule = arrayOfNulls<WinDef.HMODULE>(100 * 4)
        val lpcbNeeded = IntByReference()

        if (!Psapi.INSTANCE.EnumProcessModules(process, lphModule, lphModule.size, lpcbNeeded))
            throw IllegalStateException("EnumProcessModules failed to execute")

        var ourModule: WinDef.HMODULE? = null
        for (i in 0 until lpcbNeeded.value / 4) {
            val module = lphModule[i] ?: continue

            val lpFilename = CharArray(1024)
            val lpSize = Psapi.INSTANCE.GetModuleFileNameExW(process, module, lpFilename, lpFilename.size)
            val filename = lpFilename.copyOfRange(0, lpSize).joinToString("")

            if (filename == detail) {
                ourModule = module
                break
            }
        }

        val info = Psapi.MODULEINFO()
        if (!Psapi.INSTANCE.GetModuleInformation(process, ourModule, info, info.size()))
            throw IllegalStateException("GetModuleInformation failed to execute")

        baseAddress = info.pointer.getLong(0)
    }
}