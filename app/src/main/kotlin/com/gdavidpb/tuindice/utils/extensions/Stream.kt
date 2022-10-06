package com.gdavidpb.tuindice.utils.extensions

import java.io.InputStream
import java.io.OutputStream

fun InputStream.copyToAndClose(outputStream: OutputStream) {
    use {
        copyTo(outputStream)

        outputStream.flush()
        outputStream.close()
    }
}