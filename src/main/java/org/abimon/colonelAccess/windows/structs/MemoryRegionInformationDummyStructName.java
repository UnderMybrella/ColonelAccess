package org.abimon.colonelAccess.windows.structs;

import com.sun.jna.Structure;

import java.util.Collections;
import java.util.List;

public class MemoryRegionInformationDummyStructName extends Structure {
    public long Flags;

    public static final List<String> FIELD_ORDER = Collections.singletonList("Flags");

    public boolean isPrivate() {
        return (Flags & 1) == 1;
    }

    public boolean isMappedDataFile() {
        return ((Flags >> 1) & 1) == 1;
    }

    public boolean isMappedImage() {
        return ((Flags >> 2) & 1) == 1;
    }

    public boolean isMappedPageFile() {
        return ((Flags >> 3) & 1) == 1;
    }

    public boolean isMappedPhysical() {
        return ((Flags >> 4) & 1) == 1;
    }

    public boolean isDirectMapped() {
        return ((Flags >> 5) & 1) == 1;
    }

    public long getReserved() {
        return Flags >> 6;
    }

    @Override
    public List<String> getFieldOrder() {
        return FIELD_ORDER;
    }
}
