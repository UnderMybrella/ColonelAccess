package org.abimon.colonelAccess.windows

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.*
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import org.abimon.colonelAccess.handle.MemoryAccessor
import org.abimon.colonelAccess.handle.MemoryRegion
import org.abimon.colonelAccess.win.Colonel32
import org.abimon.colonelAccess.win.MsWinCoreMemory
import org.abimon.colonelAccess.win.WindowsMemoryAccessor
import org.abimon.colonelAccess.windows.structs.WIN32_MEMORY_REGION_INFORMATION
import org.junit.Test

internal class Colonel32Tests {
    @Test
    fun ownProcessAndPath() {
        val colonel32 = Colonel32.SAFE_INSTANCE ?: return println("Not running on Windows; returning")

        println(MemoryAccessor.detailForPID(MemoryAccessor.ourPID))
    }

    @Test
    fun memoryAccessorAcquisition() {
        val colonel32 = Colonel32.SAFE_INSTANCE ?: return println("Not running on Windows; returning")

        val accessor = WindowsMemoryAccessor(MemoryAccessor.ourPID)
        println(accessor)
    }

    @Test
    fun foreignMemoryAccessorAcquisition() {
        val colonel32 = Colonel32.SAFE_INSTANCE ?: return println("Not running on Windows; returning")

        val accessor = WindowsMemoryAccessor(15848)
        println(accessor)
    }

    @Test
    fun queryVirtualMemoryInfo() {
        val colonel32 = Colonel32.SAFE_INSTANCE ?: return println("Not running on Windows; returning")
        val handle = colonel32.OpenProcess(Kernel32.PROCESS_VM_READ or Kernel32.PROCESS_QUERY_INFORMATION, true, 15848) ?: throw IllegalAccessException("Error: No access granted to gain read permissions for 15848")

        val processNameArray = CharArray(1024)
        val size = Psapi.INSTANCE.GetModuleFileNameExW(handle, null, processNameArray, processNameArray.size)
        val detail = processNameArray.copyOfRange(0, size).joinToString("")

        val lphModule = arrayOfNulls<WinDef.HMODULE>(100 * 4)
        val lpcbNeeded = IntByReference()

        if (!Psapi.INSTANCE.EnumProcessModules(handle, lphModule, lphModule.size, lpcbNeeded))
            throw IllegalStateException("EnumProcessModules failed to execute")

        val moduleNames = lphModule.filterNotNull().map { module ->
            val regionNameArray = CharArray(1024)
            val regionNameSize = Psapi.INSTANCE.GetModuleFileNameExW(handle, module, regionNameArray, regionNameArray.size)
            val regionName = regionNameArray.copyOfRange(0, regionNameSize).joinToString("")

            return@map module to regionName
        }

        val moduleInfoMap = lphModule.filterNotNull().map { module ->
            val moduleInfo = Psapi.MODULEINFO()
            Psapi.INSTANCE.GetModuleInformation(handle, module, moduleInfo, moduleInfo.size())

            return@map module to moduleInfo
        }

        val baseAddress: Long = run {
            var ourModule: WinDef.HMODULE? = null
            for (i in 0 until lpcbNeeded.value / 4) {
                val module = lphModule[i] ?: continue

                val lpFilename = CharArray(1024)
                val lpSize = Psapi.INSTANCE.GetModuleFileNameExW(handle, module, lpFilename, lpFilename.size)
                val filename = lpFilename.copyOfRange(0, lpSize).joinToString("")

                if(filename == detail) {
                    ourModule = module
                    break
                }
            }

            val info = Psapi.MODULEINFO()
            if(!Psapi.INSTANCE.GetModuleInformation(handle, ourModule, info, info.size()))
                throw IllegalStateException("GetModuleInformation failed to execute")

            return@run Pointer.nativeValue(info.EntryPoint)
        }

        val info = WinNT.MEMORY_BASIC_INFORMATION()

        var p = Pointer(190)

        val infoSize = BaseTSD.SIZE_T(info.size().toLong())

        val regions = ArrayList<MemoryRegion>()

        while (colonel32.VirtualQueryEx(handle, p, info, infoSize).toLong() == infoSize.toLong()) {
            val regionNameArray = CharArray(1024)
            val regionNameSize = Psapi.INSTANCE.GetModuleFileNameExW(handle, info.allocationBase?.let(WinNT::HANDLE), regionNameArray, regionNameArray.size)
            val regionName = regionNameArray.copyOfRange(0,regionNameSize).joinToString("")

            regions.add(MemoryRegion(Pointer.nativeValue(p), info.regionSize.toLong(), Colonel32.allocationProtectToUnix(info.allocationProtect.toInt()), regionName))

            p = p.share(info.regionSize.toLong())
        }

        println()
    }

    @Test
    fun getAllRegions() {
        val colonel32 = Colonel32.SAFE_INSTANCE ?: return println("Not running on Windows; returning")

        val regions = WindowsMemoryAccessor(15848).getAllRegions()
    }
}