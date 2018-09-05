package org.abimon.colonelAccess.osx.structs;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class VMRegionBasicInfo extends Structure {
    public int protection;
    public int max_protection;
    public int inheritance;
    public int shared;
    public int reserved;
    public long offset;
    public int behavior;
    public int user_wired_count;

    private static final List<String> FIELD_NAMES = Arrays.asList(
            "protection",
            "max_protection",
            "inheritance",
            "shared",
            "reserved",
            "offset",
            "behavior",
            "user_wired_count"
    );

    @Override
    protected List<String> getFieldOrder() {
        return FIELD_NAMES;
    }
}
