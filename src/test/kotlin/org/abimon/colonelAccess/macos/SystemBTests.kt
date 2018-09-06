package org.abimon.colonelAccess.macos

import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import org.abimon.colonelAccess.osx.KernReturn
import org.abimon.colonelAccess.osx.SystemB
import org.abimon.colonelAccess.osx.VMInherit
import org.abimon.colonelAccess.osx.VMRegionFlavor
import org.abimon.colonelAccess.osx.structs.VMRegionBasicInfo
import org.abimon.colonelAccess.osx.structs.VMRegionSubmapInfo64
import org.junit.Test

class SystemBTests {
    @Test
    fun mach_vm_region() {
        val systemB = SystemB.SAFE_INSTANCE
        if (systemB == null) {
            println("Not running on MacOS, returning")
            return
        }

        val task = systemB.mach_task_self()

        val address = LongByReference(0)
        val size = LongByReference()

        val info = VMRegionBasicInfo()
        val infoCount = LongByReference(info.size().toLong() / 4)

        val objectName = IntByReference(0)

        val regionSuccessCode = KernReturn.valueOf(systemB.mach_vm_region(task, address, size, VMRegionFlavor.VM_REGION_BASIC_INFO_64.code, info, infoCount, objectName))

        assert(regionSuccessCode == KernReturn.KERN_SUCCESS)
    }

    @Test
    fun vm_region_recurse_64() {
        val systemB = SystemB.SAFE_INSTANCE
        if (systemB == null) {
            println("Not running on MacOS, returning")
            return
        }

        val task = systemB.mach_task_self()

        val address = LongByReference(0)
        val size = LongByReference()

        val info = VMRegionSubmapInfo64()
        val infoCount = LongByReference(info.size().toLong() / 4)

        val depth = IntByReference(1)

        val kret = KernReturn.valueOf(systemB.vm_region_recurse_64(task, address, size, depth, info, infoCount))
        assert(kret == KernReturn.KERN_SUCCESS)
    }

    @Test
    fun mach_vm_remap() {
        val systemB = SystemB.SAFE_INSTANCE
        if (systemB == null) {
            println("Not running on MacOS, returning")
            return
        }

        val task = systemB.mach_task_self()

        val address = LongByReference(0)
        val size = LongByReference()

        val info = VMRegionBasicInfo()
        val infoCount = LongByReference(info.size().toLong() / 4)

        val objectName = IntByReference(0)

        val regionSuccessCode = KernReturn.valueOf(systemB.mach_vm_region(task, address, size, VMRegionFlavor.VM_REGION_BASIC_INFO_64.code, info, infoCount, objectName))

        assert(regionSuccessCode == KernReturn.KERN_SUCCESS)

        //val newMemory = Memory(size.value)
        val newAddress = LongByReference(0)

        val curProtection = IntByReference()
        val maxProtection = IntByReference()

        val kret = KernReturn.valueOf(systemB.mach_vm_remap(task, newAddress, size.value, 0L, 1, task, address.value, false, curProtection, maxProtection, VMInherit.VM_INHERIT_SHARE.code))
        println(kret)
    }
}