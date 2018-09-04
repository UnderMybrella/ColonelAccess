package org.abimon.colonelAccess.osx

import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import org.abimon.colonelAccess.handle.MemoryAccessor
import org.abimon.colonelAccess.handle.deallocateMacOS

open class OSXMemoryAccessor(pid: Int): MemoryAccessor<KernReturn>(pid) {
    private val task: Int = run {
        val taskReference = IntByReference()
        val successCode = KernReturn.valueOf(SystemB.INSTANCE.task_for_pid(SystemB.INSTANCE.mach_task_self(), pid, taskReference))!!
        if(successCode != KernReturn.KERN_SUCCESS)
            throw IllegalStateException("Attempting to get task for PID $pid gave error code $successCode.")

        return@run taskReference.value
    }

    override fun readMemory(address: Long, size: Long): Pair<Pointer?, KernReturn?> {
        val data = PointerByReference()
        val sizeReference = LongByReference()

        val readResponse = KernReturn.valueOf(SystemB.INSTANCE.vm_read(task, address, size, data, sizeReference))

        return MacOSPointer(data.value, sizeReference.value) to readResponse
    }

    override fun readInt(address: Long): Pair<Int?, KernReturn?> {
        val (memory, error) = readMemory(address, 4)

        val num = memory?.getInt(0)
        memory?.deallocateMacOS(task)

        return num to error
    }
}