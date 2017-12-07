package org.abimon.colonelAccess.win

import com.sun.jna.Memory
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import org.abimon.colonelAccess.handle.MemoryAccessor

class WindowsMemoryAccessor(pid: Int): MemoryAccessor<Unit>(pid) {
    private val process: Pointer = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_VM_READ, true, pid)

    override fun readMemory(address: Long, size: Int): Pair<Memory?, Unit?> {
        val read = IntByReference()
        val output = Memory(size.toLong())

        Kernel32.INSTANCE.ReadProcessMemory(process, address, output, size, read)
        return output to Unit
    }
}