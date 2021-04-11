package com.gdavidpb.tuindice.data.source.storage

import android.content.Context
import com.gdavidpb.tuindice.utils.extensions.encryptedFile
import java.io.File
import java.io.InputStream
import java.io.OutputStream

open class EncryptedStorageDataSource(
        context: Context
) : ClearStorageDataSource(context) {
    override fun outputStream(path: String): OutputStream {
        val outputFile = ensurePath(path)

        if (outputFile.exists()) outputFile.delete()

        val encryptedFile = context.encryptedFile(outputFile)

        return encryptedFile.openFileOutput()
    }

    override fun inputStream(path: String): InputStream {
        val outputFile = File(root, path)

        val encryptedFile = context.encryptedFile(outputFile)

        return encryptedFile.openFileInput()
    }
}