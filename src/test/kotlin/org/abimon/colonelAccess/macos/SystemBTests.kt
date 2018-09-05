package org.abimon.colonelAccess.macos

import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.LongByReference
import org.abimon.colonelAccess.osx.KernReturn
import org.abimon.colonelAccess.osx.SystemB
import org.abimon.colonelAccess.osx.VMRegionFlavor
import org.abimon.colonelAccess.osx.structs.VMRegionBasicInfo
import org.junit.Test

class SystemBTests {
    @Test
    fun vm_region() {
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
}