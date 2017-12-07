package org.abimon.colonelAccess.osx

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference

interface SystemB: Library {
    companion object {
        val INSTANCE: SystemB = Native.loadLibrary("System.B", SystemB::class.java)
    }

    fun task_for_pid(target_tport: Int, pid: Int, t: IntByReference): Int
    fun mach_task_self(): Int

    fun vm_read(target_task: Int, address: Long, size: Int, data: PointerByReference, data_count: IntByReference): Int
}