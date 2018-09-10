package org.abimon.colonelAccess.windows.structs;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class WIN32_MEMORY_REGION_INFORMATION extends Structure {
    public Pointer AllocationBase;
    public long AllocationProtect;

    public MemoryRegionInformationDummyUnionName DUMMYUNIONNAME;

    public int RegionSize;
    public int CommitSize;

    public static final List<String> FIELD_ORDER = Arrays.asList("AllocationBase", "AllocationProtect", "DUMMYUNIONNAME", "RegionSize", "CommitSize");
    public static final List<String> DUMMY_UNION_FIELD_ORDER = Arrays.asList("Flags", "DUMMYSTRUCTNAME");

    @Override
    protected List<String> getFieldOrder() {
        return FIELD_ORDER;
    }
}
