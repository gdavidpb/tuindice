package com.gdavidpb.tuindice.data.source.storage

import android.content.Context
import com.gdavidpb.tuindice.domain.repository.LocalStorageRepository
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

open class DiskStorageDataStore(private val context: Context) : LocalStorageRepository {

    override fun putSync(name: String, inputStream: InputStream): File? {
        val outputFile = File(context.filesDir, name)

        /* Create directories to */
        outputFile.parentFile.mkdirs()

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

    override fun put(name: String, inputStream: InputStream): Single<File> {
        return Single.fromCallable { putSync(name, inputStream) }
    }

    override fun get(name: String): Maybe<InputStream> {
        return Maybe.fromCallable {
            getSync(name)!!
        }.onErrorComplete {
            it is NullPointerException
        }
    }

    override fun delete(name: String, throwOnMissing: Boolean): Completable {
        return Completable.fromCallable {
            File(context.filesDir, name).let {
                if (it.isDirectory)
                    it.deleteRecursively()
                else
                    it.delete()
            }
        }.onErrorComplete {
            !throwOnMissing
        }
    }
}