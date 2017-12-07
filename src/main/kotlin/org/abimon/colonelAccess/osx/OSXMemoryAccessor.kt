package org.abimon.colonelAccess.osx

import com.sun.jna.Memory
import com.sun.jna.ptr.IntByReference
import org.abimon.colonelAccess.handle.MemoryAccessor

class OSXMemoryAccessor(pid: Int): MemoryAccessor<KernReturn>(pid) {
    private val task: Int = run {
        val taskReference = IntByReference()
        val successCode = KernReturn.valueOf(SystemB.INSTANCE.task_for_pid(SystemB.INSTANCE.mach_task_self(), pid, taskReference))!!
        if(successCode != KernReturn.KERN_SUCCESS)
            throw IllegalStateException("Attempting to get task for PID $pid gave error code $successCode.")

        return@run taskReference.value
    }

    override fun readMemory(address: Long, size: Int): Pair<Memory?, KernReturn?> {
        val data = Memory(size.toLong())
        val sizeReference = IntByReference()

        val readResponse = KernReturn.valueOf(SystemB.INSTANCE.vm_read(task, address, size, data, sizeReference))

        return data to readResponse
    }
}