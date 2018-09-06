package org.abimon.colonelAccess.osx.structs;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class VMRegionSubmapInfo64 extends Structure {
    public int protection;
    public int max_protection;
    public int inheritance;
    public long offset;

    public int user_tag;
    public int pages_resident;
    public int pages_shared_now_private;
    public int pages_swapped_out;
    public int pages_dirtied;
    public int ref_count;
    public short shadow_depth;
    public byte external_pager;
    public byte share_mode;

    public boolean is_submap;
    public int behavior;
    public long object_id;
    public int user_wired_count;

    private static final List<String> FIELD_NAMES = Arrays.asList(
            "protection",
            "max_protection",
            "inheritance",
            "offset",

            "user_tag",
            "pages_resident",
            "pages_shared_now_private",
            "pages_swapped_out",
            "pages_dirtied",
            "ref_count",
            "shadow_depth",
            "external_pager",
            "share_mode",

            "is_submap",
            "behavior",
            "object_id",
            "user_wired_count"
    );

    @Override
    protected List<String> getFieldOrder() {
        return FIELD_NAMES;
    }
}
