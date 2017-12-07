package org.abimon.colonelAccess.win

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.StdCallLibrary

interface Kernel32: StdCallLibrary {
    companion object {
        val PROCESS_VM_READ = 0x0010
        val PROCESS_VM_WRITE = 0x0020
        val PROCESS_VM_OPERATION = 0x0008

        val INSTANCE = Native.loadLibrary("kernel32", Kernel32::class.java)
    }

    fun ReadProcessMemory(hProcess: Pointer, inBaseAddress: Long, outputBuffer: Pointer, nSize: Int, outNumberOfBytesRead: IntByReference): Boolean

    fun OpenProcess(desired: Int, inherit: Boolean, pid: Int): Pointer

    fun GetLastError(): Int
}