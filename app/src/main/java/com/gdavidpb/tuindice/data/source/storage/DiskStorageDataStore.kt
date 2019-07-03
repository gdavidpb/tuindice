package com.gdavidpb.tuindice.data.source.storage

import android.content.Context
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream

open class DiskStorageDataStore(
        private val context: Context
) : LocalStorageRepository {

    override fun putSync(name: String, inputStream: InputStream): File? {
        val outputFile = File(context.filesDir, name)

        /* Create directories to */
        outputFile.parentFile?.mkdirs()

        val outputStream = FileOutputStream(outputFile)

        inputStream.copyTo(outputStream)

        outputStream.flush()
        outputStream.close()

        return outputFile
    }

    override fun getSync(name: String): InputStream? {
        val inputFile = File(context.filesDir, name)

        return if (inputFile.exists())
            inputFile.inputStream()
        else
            null
    }

    override suspend fun put(name: String, inputStream: InputStream): File {
        return putSync(name, inputStream)!!
    }

    override suspend fun get(name: String): InputStream {
        return getSync(name)!!
    }

    override suspend fun delete(name: String) {
        runCatching {
            File(context.filesDir, name).let {
                if (it.isDirectory)
                    it.deleteRecursively()
                else
                    it.delete()
            }
        }.onFailure { throwable ->
            if (throwable !is FileNotFoundException)
                throw throwable
        }
    }
}