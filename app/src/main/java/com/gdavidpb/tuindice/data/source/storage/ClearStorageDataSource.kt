package com.gdavidpb.tuindice.data.source.storage

import android.content.Context
import com.gdavidpb.tuindice.domain.repository.StorageRepository
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

open class ClearStorageDataSource(
        protected val context: Context
) : StorageRepository<File> {

    protected val root: File = context.filesDir

    protected fun ensurePath(path: String) = File(root, path).also { it.parentFile?.mkdirs() }

    override fun get(path: String): File {
        return File(root, path)
    }

    override fun create(path: String): File {
        val outputFile = ensurePath(path)

        outputFile.createNewFile()

        return outputFile
    }

    override fun outputStream(path: String): OutputStream {
        val file = ensurePath(path)

        return file.outputStream()
    }

    override fun inputStream(path: String): InputStream {
        val file = File(root, path)

        return file.inputStream()
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

    override fun clear() {
        runCatching {
            root.deleteRecursively()
            root.mkdir()
        }.onFailure { throwable ->
            if (throwable !is FileNotFoundException) throw throwable
        }
    }

    override fun encryptedInputStream(path: String): InputStream {
        throw NotImplementedError()
    }

    override fun encryptedOutputStream(path: String): OutputStream {
        throw NotImplementedError()
    }
}