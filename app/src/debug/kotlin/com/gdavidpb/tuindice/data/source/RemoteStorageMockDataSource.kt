package com.gdavidpb.tuindice.data.source

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.gdavidpb.tuindice.base.domain.repository.RemoteStorageRepository
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

@Suppress("BlockingMethodInNonBlockingContext")
open class RemoteStorageMockDataSource(
        context: Context
) : RemoteStorageRepository {

    private val root = File(context.filesDir, "remote")

    override suspend fun removeResource(resource: String) {
        runCatching {
            File(root, resource).let {
                if (it.isDirectory)
                    it.deleteRecursively()
                else
                    it.delete()
            }
        }.onFailure { throwable ->
            if (throwable !is FileNotFoundException) throw throwable
        }
    }

    override suspend fun resolveResource(resource: String): Uri {
        val file = File(root, resource)

        return if (file.exists()) file.toUri() else "".toUri()
    }

    override suspend fun uploadResource(resource: String, stream: InputStream): Uri {
        val outputFile = File(root, resource)

        outputFile.parentFile?.mkdirs()

        outputFile.outputStream().use { outputStream ->
            stream.copyTo(outputStream)
            outputStream.flush()
        }

        return outputFile.toUri()
    }
}