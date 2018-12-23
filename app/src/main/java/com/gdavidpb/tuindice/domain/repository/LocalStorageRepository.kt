package com.gdavidpb.tuindice.domain.repository

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import java.io.File
import java.io.InputStream

interface LocalStorageRepository {
    fun put(name: String, inputStream: InputStream): Single<File>
    fun get(name: String): Maybe<InputStream>
    fun delete(name: String): Completable
}