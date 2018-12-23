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
    override fun put(name: String, inputStream: InputStream): Single<File> {
        return Single.create { emitter ->
            val outputFile = File(context.filesDir, name)

            runCatching {
                val outputStream = FileOutputStream(outputFile)

                inputStream.copyTo(outputStream)

                outputStream.flush()
                outputStream.close()
            }.onSuccess {
                emitter.onSuccess(outputFile)
            }.onFailure {
                emitter.onError(it)
            }
        }
    }

    override fun get(name: String): Maybe<InputStream> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(name: String): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}