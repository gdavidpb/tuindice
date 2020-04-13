package com.gdavidpb.tuindice.data.source.storage

import android.content.Context
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

open class DiskStorageDataStore(
        context: Context
) : LocalStorageRepository {

    private val root = context.filesDir

    override fun create(path: String): File {
        val outputFile = File(root, path)

        outputFile.parentFile?.mkdirs()

        outputFile.createNewFile()

        return outputFile
    }

    override fun put(path: String, inputStream: InputStream): File {
        val outputFile = File(root, path)

        outputFile.parentFile?.mkdirs()

        val outputStream = outputFile.outputStream()

        inputStream.copyTo(outputStream)

        outputStream.flush()
        outputStream.close()

        return outputFile
    }

    override fun inputStream(path: String): InputStream {
        val inputFile = File(root, path)

        inputFile.parentFile?.mkdirs()

        return inputFile.inputStream()
    }

    override fun outputStream(path: String): OutputStream {
        val outputFile = File(root, path)

        outputFile.parentFile?.mkdirs()

        return outputFile.outputStream()
    }

    override fun delete(path: String) {
        runCatching {
            File(root, path).let {
                if (it.isDirectory)
                    it.deleteRecursively()
                else
                    it.delete()
            }
        }.onFailure { throwable ->
            if (throwable !is FileNotFoundException) throw throwable
        }
    }

    override fun exists(path: String): Boolean {
        val file = File(root, path)

        return file.exists()
    }

    override fun get(path: String): File {
        return File(root, path)
    }

    override fun clear() {
        runCatching {
            root.deleteRecursively()
            root.mkdir()
        }.onFailure { throwable ->
            if (throwable !is FileNotFoundException) throw throwable
        }
    }
}