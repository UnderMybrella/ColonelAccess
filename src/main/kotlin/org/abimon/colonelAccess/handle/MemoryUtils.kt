package org.abimon.colonelAccess.handle

import com.sun.jna.Pointer
import org.abimon.colonelAccess.osx.KernReturn
import org.abimon.colonelAccess.osx.MacOSPointer

fun Pointer.deallocateMacOS(task: Int): KernReturn? = (this as? MacOSPointer)?.deallocate(task)