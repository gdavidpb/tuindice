package com.gdavidpb.tuindice.data.source.storage

import android.net.Uri
import com.gdavidpb.tuindice.domain.repository.RemoteStorageRepository
import com.gdavidpb.tuindice.utils.extensions.await
import com.google.firebase.storage.FirebaseStorage
import java.io.InputStream

open class FirebaseStorageDataStore(
        private val storage: FirebaseStorage
) : RemoteStorageRepository {
    override suspend fun resolveResource(resource: String): Uri {
        val reference = storage.getReferenceFromUrl(resource)

        return reference.downloadUrl.await()
    }

    override suspend fun uploadResource(resource: String, stream: InputStream): Uri {
        val reference = storage.reference.child(resource)

        reference.putStream(stream).await()

        return reference.downloadUrl.await()
    }
}