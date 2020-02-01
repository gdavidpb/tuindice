package com.gdavidpb.tuindice.domain.repository

import java.io.File
import java.io.InputStream

interface LocalStorageRepository {
    fun put(name: String, inputStream: InputStream): File
    fun get(name: String): InputStream
    fun delete(name: String)
    fun exists(name: String): Boolean
    fun getPath(name: String): String
}