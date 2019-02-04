package com.gdavidpb.tuindice.domain.repository

import java.io.File
import java.io.InputStream

interface LocalStorageRepository {
    suspend fun put(name: String, inputStream: InputStream): File
    suspend fun get(name: String): InputStream
    suspend fun delete(name: String)

    fun putSync(name: String, inputStream: InputStream): File?
    fun getSync(name: String): InputStream?
}