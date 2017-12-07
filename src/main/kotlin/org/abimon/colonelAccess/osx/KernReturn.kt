package org.abimon.colonelAccess.osx

enum class KernReturn(val errorCode: Int) {
    KERN_SUCCESS(0),
    KERN_INVALID_ADDRESS(1),
    KERN_PROTECTION_FAILURE(2),
    KERN_NO_SPACE(3),
    KERN_INVALID_ARGUMENT(4),
    KERN_FAILURE(5);

    companion object {
        fun valueOf(errorCode: Int): KernReturn? = values().firstOrNull { kernReturn -> kernReturn.errorCode == errorCode }
    }
}