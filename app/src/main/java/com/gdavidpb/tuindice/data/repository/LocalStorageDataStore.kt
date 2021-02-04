package com.gdavidpb.tuindice.data.repository

import java.io.File
import java.io.InputStream
import java.io.OutputStream

interface LocalStorageDataStore {
    fun get(path: String): File
    fun create(path: String): File
    fun inputStream(path: String): InputStream
    fun outputStream(path: String): OutputStream
    fun delete(path: String)
    fun exists(path: String): Boolean
    fun clear()
}