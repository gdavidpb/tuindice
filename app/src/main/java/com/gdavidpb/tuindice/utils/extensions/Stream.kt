package com.gdavidpb.tuindice.utils.extensions

import java.io.InputStream
import java.io.OutputStream

fun InputStream.copyTo(
        outputStream: OutputStream,
        autoFlush: Boolean,
        autoClose: Boolean
) {
    use {
        copyTo(outputStream)

        if (autoFlush) outputStream.flush()
        if (autoClose) outputStream.close()
    }
}