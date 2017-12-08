package org.abimon.colonelAccess.win

import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Psapi
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import org.abimon.colonelAccess.handle.MemoryAccessor

class WindowsMemoryAccessor(pid: Int): MemoryAccessor<Unit>(pid) {
    private val process: WinNT.HANDLE = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_VM_READ or Kernel32.PROCESS_QUERY_INFORMATION, true, pid)
    private val baseAddress: Long

    override fun readMemory(address: Long, size: Int): Pair<Memory?, Unit?> {
        val read = IntByReference()
        val output = Memory(size.toLong())

        Kernel32.INSTANCE.ReadProcessMemory(process, Pointer.createConstant(baseAddress + address), output, size, read)
        return output to Unit
    }

    init {
        val processNameArray = CharArray(1024)
        val size = Psapi.INSTANCE.GetProcessImageFileName(process, processNameArray, processNameArray.size)
        val processName = processNameArray.copyOfRange(0, size).joinToString("").substringAfterLast('\\')

        val lphModule = arrayOfNulls<WinDef.HMODULE>(100 * 4)
        val lpcbNeeded = IntByReference()

        if (!Psapi.INSTANCE.EnumProcessModules(process, lphModule, lphModule.size, lpcbNeeded))
            throw IllegalStateException("EnumProcessModules failed to execute")

        var drModule: WinDef.HMODULE? = null
        for (i in 0 until lpcbNeeded.value / 4) {
            val module = lphModule[i] ?: continue

            val lpFilename = CharArray(1024)
            val lpSize = Psapi.INSTANCE.GetModuleFileNameExW(process, module, lpFilename, lpFilename.size)
            val filename = lpFilename.copyOfRange(0, lpSize).joinToString("")

            if(filename.contains(processName)) {
                drModule = module
                break
            }
        }

        val info = Psapi.MODULEINFO()
        if(!Psapi.INSTANCE.GetModuleInformation(process, drModule, info, info.size()))
            throw IllegalStateException("GetModuleInformation failed to execute")

        baseAddress = info.pointer.getLong(0)
    }
}