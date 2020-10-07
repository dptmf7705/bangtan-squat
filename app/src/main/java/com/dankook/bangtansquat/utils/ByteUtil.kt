package com.dankook.bangtansquat.utils

import java.nio.ByteBuffer

fun ByteBuffer.toByteArray(): ByteArray {
    rewind()
    return ByteArray(remaining()).let {
        get(it)
        it
    }
}