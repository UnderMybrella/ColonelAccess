package org.abimon.colonelAccess.osx

import com.sun.jna.Pointer

class MacOSPointer(pointer: Pointer, val size: Long): Pointer(Pointer.nativeValue(pointer)) {
    fun deallocate(task: Int): KernReturn? = KernReturn.valueOf(SystemB.INSTANCE.vm_deallocate(task, this.peer, size))
}