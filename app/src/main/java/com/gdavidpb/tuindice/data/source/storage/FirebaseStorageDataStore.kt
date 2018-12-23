package com.gdavidpb.tuindice.data.source.storage

import android.net.Uri
import com.gdavidpb.tuindice.domain.repository.RemoteStorageRepository
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.Single

open class FirebaseStorageDataStore(
        private val storage: FirebaseStorage
) : RemoteStorageRepository {
    override fun resolveResource(resource: String): Single<Uri> {
        return Single.create { emitter ->
            storage.getReferenceFromUrl(resource).downloadUrl
                    .addOnSuccessListener {
                        emitter.onSuccess(it)
                    }.addOnFailureListener {
                        emitter.onError(it)
                    }
        }
    }
}