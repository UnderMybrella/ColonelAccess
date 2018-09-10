package org.abimon.colonelAccess.win

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions
import org.abimon.colonelAccess.windows.structs.WIN32_MEMORY_REGION_INFORMATION

interface MsWinCoreMemory: Library {
    //
    companion object {
        val SAFE_INSTANCE: MsWinCoreMemory? = try {  Native.loadLibrary("api-ms-win-core-memory-l1-1-4", MsWinCoreMemory::class.java, W32APIOptions.DEFAULT_OPTIONS) } catch (ule: UnsatisfiedLinkError) { null }
        val INSTANCE: MsWinCoreMemory by lazy { SAFE_INSTANCE!! }
    }

    fun QueryVirtualMemoryInformation(Handle: WinNT.HANDLE, VirtualAddress: Pointer, MemoryInformationClass: Int, MemoryInformation: WIN32_MEMORY_REGION_INFORMATION, MemoryInformationSize: Int, ReturnSize: IntByReference?): Boolean
}