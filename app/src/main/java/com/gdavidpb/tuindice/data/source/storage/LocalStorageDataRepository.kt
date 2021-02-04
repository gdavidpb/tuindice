package com.gdavidpb.tuindice.data.source.storage

import com.gdavidpb.tuindice.domain.repository.StorageRepository
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class LocalStorageDataRepository(
        private val factory: LocalStorageDataStoreFactory
) : StorageRepository<File> {
    override fun get(path: String): File {
        return factory.retrieveClear().get(path)
    }

    override fun create(path: String): File {
        return factory.retrieveClear().create(path)
    }

    override fun inputStream(path: String): InputStream {
        return factory.retrieveClear().inputStream(path)
    }

    override fun outputStream(path: String): OutputStream {
        return factory.retrieveClear().outputStream(path)
    }

    override fun encryptedInputStream(path: String): InputStream {
        return factory.retrieveEncrypted().inputStream(path)
    }

    override fun encryptedOutputStream(path: String): OutputStream {
        return factory.retrieveEncrypted().outputStream(path)
    }

    override fun delete(path: String) {
        factory.retrieveClear().delete(path)
    }

    override fun exists(path: String): Boolean {
        return factory.retrieveClear().exists(path)
    }

    override fun clear() {
        factory.retrieveClear().clear()
    }

}