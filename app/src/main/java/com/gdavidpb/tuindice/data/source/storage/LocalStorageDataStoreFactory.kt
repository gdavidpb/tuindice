package com.gdavidpb.tuindice.data.source.storage

import com.gdavidpb.tuindice.data.repository.LocalStorageDataStore

open class LocalStorageDataStoreFactory(
        private val clearDataStore: ClearStorageDataStore,
        private val encryptedDataStore: EncryptedStorageDataStore
) {
    fun retrieveClear(): LocalStorageDataStore = clearDataStore
    fun retrieveEncrypted(): LocalStorageDataStore = encryptedDataStore
}