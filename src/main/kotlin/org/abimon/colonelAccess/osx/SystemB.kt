package org.abimon.colonelAccess.osx

import com.sun.jna.*
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import java.nio.ByteBuffer

interface SystemB: Library {
    companion object {
        val SAFE_INSTANCE: SystemB? = try { Native.loadLibrary("System.B", SystemB::class.java) } catch (ule: UnsatisfiedLinkError) { null }
        val INSTANCE: SystemB by lazy { SAFE_INSTANCE!! }

        val PATH_MAX = 1024L

        val VM_FLAGS_FIXED      = 0
        val VM_FLAGS_ANYWHERE   = 1
        val VM_FLAGS_PURABLE    = 2

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
    fun getpid(): Int

    fun mach_vm_read(target_task: Int, address: Long, size: Long, data: PointerByReference, data_count: LongByReference): Int
    fun mach_vm_write(target_task: Int, address: Long, data: ByteBuffer, data_count: Long): Int
    fun mach_vm_write(target_task: Int, address: Long, data: Pointer, data_count: Long): Int

    fun mach_vm_allocate(target_task: Int, address: LongByReference, size: Long, flags: Int): Int
    fun mach_vm_deallocate(target_task: Int, address: Long, size: Long): Int

    fun mach_vm_remap(target_task: Int, target_address: LongByReference, size: Long, mask: Long, flags: Int, source_task: Int, source_address: Long, copy: Boolean, cur_protection: IntByReference, max_protection: IntByReference, inheritance: Int): Int

    fun mach_vm_region(target_task: Int, address: LongByReference, size: LongByReference, flavor: Int, info: Structure, info_count: LongByReference, object_name: IntByReference): Int
    fun vm_region_recurse_64(target_task: Int, address: LongByReference, size: LongByReference, depth: IntByReference, info: Structure, info_count: LongByReference): Int

    fun proc_pidpath(pid: Int, buffer: Pointer, buffersize: Int): Int
    fun proc_regionfilename(pid: Int, address: Long, buffer: Pointer, buffersize: Int): Int
}