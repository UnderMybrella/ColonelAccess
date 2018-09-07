package org.abimon.colonelAccess.osx

import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import com.sun.jna.ptr.PointerByReference
import org.abimon.colonelAccess.handle.MemoryAccessor
import org.abimon.colonelAccess.handle.MemoryRegion
import org.abimon.colonelAccess.osx.structs.VMRegionBasicInfo
import org.abimon.colonelAccess.osx.structs.VMRegionSubmapInfo64

open class OSXMemoryAccessor(pid: Int): MemoryAccessor<KernReturn, MacOSPointer>(pid, KernReturn::class.java, MacOSPointer::class.java) {
    protected val task: Int = run {
        val taskReference = IntByReference()
        val successCode = KernReturn.valueOf(SystemB.INSTANCE.task_for_pid(SystemB.INSTANCE.mach_task_self(), pid, taskReference) and 0x000000FF)
        if(successCode != KernReturn.KERN_SUCCESS)
            throw IllegalStateException("Attempting to get task for PID $pid gave error code $successCode.")

        return@run taskReference.value
    }
    protected val ourTask: Int = SystemB.INSTANCE.mach_task_self()

    override val detail: String = SystemB.proc_pidpath(pid).first

    override fun readMemory(address: Long, size: Long): Pair<MacOSPointer?, KernReturn?> {
        val addr = LongByReference()
        val allocateResponse = KernReturn.valueOf(SystemB.INSTANCE.mach_vm_allocate(ourTask, addr, size, SystemB.VM_FLAGS_ANYWHERE) and 0x000000FF)
        if (allocateResponse == KernReturn.KERN_SUCCESS) {
            val curProtection = IntByReference()
            val maxProtection = IntByReference()

            val kret = KernReturn.valueOf(SystemB.INSTANCE.mach_vm_remap(ourTask, addr, size, 0L, 1, task, address, true, curProtection, maxProtection, VMInherit.VM_INHERIT_SHARE.code) and 0x000000FF)

            return MacOSPointer(addr.value, size) to kret
        } else if (allocateResponse != KernReturn.KERN_NO_SPACE) {
            println("Allocating failed with $allocateResponse, trying a manual read")
            val data = PointerByReference()
            val sizeReference = LongByReference()

            val readResponse = KernReturn.valueOf(SystemB.INSTANCE.mach_vm_read(task, address, size, data, sizeReference) and 0x000000FF)

            return (data.value?.let { pointer -> MacOSPointer(pointer, sizeReference.value) }) to readResponse
        } else {
            return null to allocateResponse
        }
    }

    override fun readInt(address: Long): Pair<Int?, KernReturn?> {
        val (memory, error) = readMemory(address, 4)

        val num = memory?.getInt(0)
        memory?.deallocate(task)

        return num to error
    }

    override fun deallocateOurMemory(pointer: MacOSPointer) = pointer.deallocate(task)

    override fun getNextRegion(address: Long): Pair<MemoryRegion?, KernReturn?> {
        val addressReference = LongByReference(address)
        val size = LongByReference()

        val info = VMRegionBasicInfo()
        val infoCount = LongByReference(info.size().toLong() / 4)

        val objectName = IntByReference(0)

        val regionSuccessCode = KernReturn.valueOf(SystemB.INSTANCE.mach_vm_region(task, addressReference, size, VMRegionFlavor.VM_REGION_BASIC_INFO_64.code, info, infoCount, objectName) and 0x000000FF)

        if (regionSuccessCode == KernReturn.KERN_SUCCESS) {
            val (detail) = SystemB.proc_regionfilename(pid, addressReference.value)

            return MemoryRegion(addressReference.value, size.value, info.protection, detail) to regionSuccessCode
        }

        return null to regionSuccessCode
    }

    override fun getAllRegions(): Array<MemoryRegion> {
        val regions: MutableList<MemoryRegion> = ArrayList()

        val address = LongByReference(0)
        val size = LongByReference()

        val info = VMRegionSubmapInfo64()
        val infoCount = LongByReference(info.size().toLong() / 4)

        val depth = IntByReference(1)

        var kret: KernReturn? = KernReturn.KERN_SUCCESS

        while (kret == KernReturn.KERN_SUCCESS) {
            kret = KernReturn.valueOf(SystemB.INSTANCE.vm_region_recurse_64(task, address, size, depth, info, infoCount) and 0x000000FF)

            if (info.is_submap) {
                depth.value++
            }

            regions.add(MemoryRegion(address.value, size.value, info.protection, SystemB.proc_regionfilename(pid, address.value).first))
            address.value += size.value
        }

        return regions.toTypedArray()
    }
}