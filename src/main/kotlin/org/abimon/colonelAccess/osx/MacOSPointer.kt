package org.abimon.colonelAccess.osx

import com.sun.jna.Pointer

class MacOSPointer(peer: Long, private val size: Long): Pointer(peer) {
    constructor(pointer: Pointer, size: Long): this(Pointer.nativeValue(pointer), size)

    fun deallocate(task: Int): KernReturn? = KernReturn.valueOf(SystemB.INSTANCE.mach_vm_deallocate(task, this.peer, size))
}