package com.gdavidpb.tuindice.data.source.firestore

import com.gdavidpb.tuindice.domain.repository.RemoteDatabaseRepository
import com.gdavidpb.tuindice.utils.COLLECTION_USER
import com.gdavidpb.tuindice.utils.FIELD_USER_TOKEN
import com.gdavidpb.tuindice.utils.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId

open class FirestoreDataStore(
        private val firebaseAuth: FirebaseAuth,
        private val firebaseFirestore: FirebaseFirestore,
        private val firebaseInstanceId: FirebaseInstanceId
) : RemoteDatabaseRepository {
    override suspend fun setToken() {
        val uid = firebaseAuth.uid ?: return

        val userRef = firebaseFirestore.collection(COLLECTION_USER).document(uid)

        val token = firebaseInstanceId.instanceId.await().token

        //todo userRef.update(FIELD_USER_TOKEN, token).await()
    }
}