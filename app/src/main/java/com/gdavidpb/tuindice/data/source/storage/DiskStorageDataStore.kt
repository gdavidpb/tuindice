package com.gdavidpb.tuindice.data.source.storage

import android.content.Context
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream

open class DiskStorageDataStore(
        context: Context
) : LocalStorageRepository {

    private val root = context.filesDir

    override fun put(name: String, inputStream: InputStream): File {
        val outputFile = File(root, name)

        /* Create directories to */
        outputFile.parentFile?.mkdirs()

        val outputStream = FileOutputStream(outputFile)

        inputStream.copyTo(outputStream)

        outputStream.flush()
        outputStream.close()

        return outputFile
    }

    override fun get(name: String): InputStream {
        val inputFile = File(root, name)

        return inputFile.inputStream()
    }

    override fun delete(name: String) {
        runCatching {
            File(root, name).let {
                if (it.isDirectory)
                    it.deleteRecursively()
                else
                    it.delete()
            }
        }.onFailure { throwable ->
            if (throwable !is FileNotFoundException) throw throwable
        }
    }

    override fun exists(name: String): Boolean {
        val file = File(root, name)

        return file.exists()
    }

    override fun getPath(name: String): String {
        val file = File(root, name)

        return file.path
    }
}