package org.abimon.colonelAccess.win

import com.sun.jna.Memory
import com.sun.jna.Pointer

open class WinMemory(size: Long): Memory(size) {
    protected open var allocated: Boolean = true

    public open var readSize: Long = size

    override fun boundsCheck(off: Long, sz: Long) {
        if (off < 0) {
            throw IndexOutOfBoundsException("Invalid offset: $off")
        }
        if (off + sz > readSize) {
            val msg = ("Bounds exceeds available space : size="
                    + readSize + ", offset=" + (off + sz))
            throw IndexOutOfBoundsException(msg)
        }
    }

    public override fun dispose() {
        if (allocated) {
            super.dispose()
            allocated = false
        }
    }
}