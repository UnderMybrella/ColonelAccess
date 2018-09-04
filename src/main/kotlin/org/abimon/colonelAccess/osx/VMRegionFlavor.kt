package org.abimon.colonelAccess.osx

enum class VMRegionFlavor(val code: Int) {
    VM_REGION_BASIC_INFO_64(9),
    VM_REGION_BASIC_INFO(10),
    VM_REGION_EXTENDED_INFO(13),
    VM_REGION_TOP_INFO(12);

    companion object {
        fun valueOf(code: Int): VMRegionFlavor? = VMRegionFlavor.values().firstOrNull { flavor -> flavor.code == code }
    }
}