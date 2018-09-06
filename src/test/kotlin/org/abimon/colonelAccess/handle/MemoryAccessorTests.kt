package org.abimon.colonelAccess.handle

import org.junit.Test

internal class MemoryAccessorTests {
    @Test
    fun deallocateTests() {
        val accessor = MemoryAccessor.accessorForSystem(MemoryAccessor.ourPID)
        val (region) = accessor.getNextRegion(0L)

        assert(region != null)

        val (memory) = accessor.readMemory(region!!.start, 4L)

        assert(memory != null)

        accessor.deallocateMemory(memory!!)
    }
}