package com.gdavidpb.tuindice.data.source.firestore

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.utils.COLLECTION_USER
import com.gdavidpb.tuindice.utils.FIELD_USER_TOKEN
import com.gdavidpb.tuindice.utils.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

open class FirestoreDataStore(
        private val auth: FirebaseAuth,
        private val firestore: FirebaseFirestore
) : DatabaseRepository {
    override suspend fun updateAccount(account: Account) {
        val uid = auth.uid ?: return

        val userRef = firestore.collection(COLLECTION_USER).document(uid)

        userRef.set(account, SetOptions.merge()).await()
    }

    override suspend fun setToken(token: String) {
        val uid = auth.uid ?: return

        val userRef = firestore.collection(COLLECTION_USER).document(uid)

        val values = mapOf(
                FIELD_USER_TOKEN to token
        )

        userRef.set(values, SetOptions.merge()).await()
    }
}