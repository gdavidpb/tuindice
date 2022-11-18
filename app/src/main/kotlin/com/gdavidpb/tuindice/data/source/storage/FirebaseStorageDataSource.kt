package com.gdavidpb.tuindice.data.source.storage

import android.net.Uri
import com.gdavidpb.tuindice.base.domain.repository.RemoteStorageRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.InputStream

class FirebaseStorageDataSource(
    private val storage: FirebaseStorage
) : RemoteStorageRepository {
    override suspend fun removeResource(resource: String) {
        val reference = storage.getReference(resource)

        reference.delete().await()
    }

    override suspend fun resolveResource(resource: String): Uri {
        val reference = storage.getReference(resource)

        return reference.downloadUrl.await()
    }

    override suspend fun uploadResource(resource: String, stream: InputStream): Uri {
        val reference = storage.reference.child(resource)

        reference.putStream(stream).await()

        return reference.downloadUrl.await()
    }
}