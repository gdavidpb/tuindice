package com.gdavidpb.tuindice.data.source.storage

import android.net.Uri
import com.gdavidpb.tuindice.utils.extensions.await
import com.gdavidpb.tuindice.domain.repository.RemoteStorageRepository
import com.google.firebase.storage.FirebaseStorage

open class FirebaseStorageDataStore(
        private val storage: FirebaseStorage
) : RemoteStorageRepository {
    override suspend fun resolveResource(resource: String): Uri {
        return storage.getReferenceFromUrl(resource).downloadUrl.await()
    }
}