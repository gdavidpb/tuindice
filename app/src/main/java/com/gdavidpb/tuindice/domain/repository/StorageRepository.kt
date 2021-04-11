package com.gdavidpb.tuindice.domain.repository

import java.io.InputStream
import java.io.OutputStream

interface StorageRepository<T> {
    fun get(path: String): T
    fun create(path: String): T
    fun inputStream(path: String): InputStream
    fun outputStream(path: String): OutputStream
    fun delete(path: String)
    fun exists(path: String): Boolean
    fun clear()
}