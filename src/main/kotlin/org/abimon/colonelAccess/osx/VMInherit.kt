package org.abimon.colonelAccess.osx

enum class VMInherit(val code: Int) {
    VM_INHERIT_SHARE(0),
    VM_INHERIT_COPY(1),
    VM_INHERIT_NONE(2),
    VM_INHERIT_DONATE_COPY(3);

    companion object {
        fun valueOf(code: Int): VMInherit? = VMInherit.values().firstOrNull { flavor -> flavor.code == code }
    }
}