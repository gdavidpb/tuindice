package com.gdavidpb.tuindice.data.source.storage

import com.gdavidpb.tuindice.domain.repository.StorageRepository
import java.io.File

open class LocalStorageDataSourceFactory(
        private val clearDataSource: ClearStorageDataSource,
        private val encryptedDataSource: EncryptedStorageDataSource
) {
    fun retrieveClear(): StorageRepository<File> = clearDataSource
    fun retrieveEncrypted(): StorageRepository<File> = encryptedDataSource
}