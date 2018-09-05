package org.abimon.colonelAccess.osx

import com.sun.jna.*
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference

interface SystemB: Library {
    companion object {
        val SAFE_INSTANCE: SystemB? = try { Native.loadLibrary("System.B", SystemB::class.java) } catch (ule: UnsatisfiedLinkError) { null }
        val INSTANCE: SystemB = SAFE_INSTANCE!!

        val PATH_MAX = 1024L

        //Slightly nicer native methods
        fun proc_pidpath(pid: Int): Pair<String, Int> {
            val buffer = Memory(PATH_MAX)
            val kret = INSTANCE.proc_pidpath(pid, buffer, buffer.size().toInt())

            return buffer.getString(0) to kret
        }

        fun proc_regionfilename(pid: Int, address: Long): Pair<String, Int> {
            val buffer = Memory(PATH_MAX)
            val kret = INSTANCE.proc_regionfilename(pid, address, buffer, buffer.size().toInt())

            return buffer.getString(0) to kret
        }
    }

    fun task_for_pid(target_tport: Int, pid: Int, t: IntByReference): Int
    fun mach_task_self(): Int

    fun mach_vm_read(target_task: Int, address: Long, size: Long, data: PointerByReference, data_count: LongByReference): Int
    fun mach_vm_write(target_task: Int, address: Long, size: Long, data: PointerByReference, data_count: LongByReference): Int

    fun mach_vm_allocate(target_task: Int, address: Long, size: Long, anywhere: Boolean): Int
    fun mach_vm_deallocate(target_task: Int, address: Long, size: Long): Int

    fun mach_vm_region(target_task: Int, address: LongByReference, size: LongByReference, flavor: Int, info: Structure, info_count: LongByReference, object_name: IntByReference): Int

    fun proc_pidpath(pid: Int, buffer: Pointer, buffersize: Int): Int
    fun proc_regionfilename(pid: Int, address: Long, buffer: Pointer, buffersize: Int): Int
}