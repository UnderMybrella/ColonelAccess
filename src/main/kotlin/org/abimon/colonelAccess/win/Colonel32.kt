package org.abimon.colonelAccess.win

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import com.sun.jna.win32.W32APIOptions
import org.abimon.colonelAccess.osx.SystemB
import org.abimon.colonelAccess.windows.structs.WIN32_MEMORY_REGION_INFORMATION

//Yes I think I'm funny
interface Colonel32: Kernel32 {
    companion object {
        val SAFE_INSTANCE: Colonel32? = try {  Native.loadLibrary("kernel32", Colonel32::class.java, W32APIOptions.DEFAULT_OPTIONS) } catch (ule: UnsatisfiedLinkError) { null }
        val INSTANCE: Colonel32 by lazy { SAFE_INSTANCE!! }

        fun allocationProtectToUnix(allocationProtect: Int): Int {
            return when (allocationProtect) {
                0x00 -> 0
                0x01 -> 0
                0x02 -> 1
                0x04 -> 1 or 2
                0x08 -> 1
                0x10 -> 4
                0x20 -> 1 or 4
                0x40 -> 1 or 2 or 4
                0x80 -> 1 or 4
                0x40000000 -> 0
                else -> 0
            }
        }
    }

    fun GetModuleHandleEx(flags: WinDef.DWORD, lpModuleName: String, phModule: PointerByReference): Boolean
}