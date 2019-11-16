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

    override fun putSync(name: String, inputStream: InputStream): File? {
        val outputFile = File(root, name)

        /* Create directories to */
        outputFile.parentFile?.mkdirs()

        val outputStream = FileOutputStream(outputFile)

        inputStream.copyTo(outputStream)

        outputStream.flush()
        outputStream.close()

        return outputFile
    }

    override fun getSync(name: String): InputStream? {
        val inputFile = File(root, name)

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

    override fun getFile(name: String): File {
        return File(root, name)
    }

    override suspend fun delete(name: String) {
        runCatching {
            File(root, name).let {
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