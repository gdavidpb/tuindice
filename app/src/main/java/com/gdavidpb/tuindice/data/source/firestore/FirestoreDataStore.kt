package com.gdavidpb.tuindice.data.source.firestore

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import com.gdavidpb.tuindice.domain.model.service.DstRecord
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.*

open class FirestoreDataStore(
        private val auth: FirebaseAuth,
        private val firestore: FirebaseFirestore
) : DatabaseRepository {
    override suspend fun getAccountByUId(uid: String, lastUpdate: Date): Account {
        return firestore
                .collection(COLLECTION_USER)
                .document(uid)
                .get()
                .await()
                .toAccountEntity()
                .toAccount(lastUpdate)
    }

    override suspend fun getActiveAccount(lastUpdate: Date): Account? {
        return auth.uid?.let { uid ->
            getAccountByUId(uid, lastUpdate)
        }
    }

    override suspend fun updateAuthData(data: DstAuth) {
        val uid = auth.uid ?: return

        val userRef = firestore
                .collection(COLLECTION_USER)
                .document(uid)

        userRef.set(data, SetOptions.merge()).await()
    }

    override suspend fun updatePersonalData(data: DstPersonal) {
        val uid = auth.uid ?: return

        val userRef = firestore
                .collection(COLLECTION_USER)
                .document(uid)

        userRef.set(data, SetOptions.merge()).await()
    }

    override suspend fun updateRecordData(data: DstRecord) {
        val uid = auth.uid ?: return

        val batch = firestore.batch()

        val userRef = firestore
                .collection(COLLECTION_USER)
                .document(uid)

        batch.set(userRef, data.stats, SetOptions.merge())

        data.quarters.forEach { dstQuarter ->
            val quarter = dstQuarter.toQuarterEntity(uid)

            val quarterId = quarter.generateId()

            val quarterRef = firestore
                    .collection(COLLECTION_QUARTER)
                    .document(quarterId)

            batch.set(quarterRef, quarter)

            dstQuarter.subjects.forEach { dstSubject ->
                val subject = dstSubject.toSubjectEntity(uid = uid, qid = quarterId)

                val subjectId = subject.generateId()

                val subjectRef = firestore
                        .collection(COLLECTION_SUBJECT)
                        .document(subjectId)

                batch.set(subjectRef, subject)
            }
        }

        batch.commit().await()
    }

    override suspend fun updateToken(token: String) {
        val uid = auth.uid ?: return

        val userRef = firestore.collection(COLLECTION_USER).document(uid)

        val values = mapOf(
                FIELD_USER_TOKEN to token
        )

        userRef.set(values, SetOptions.merge()).await()
    }

    override suspend fun <T> remoteTransaction(transaction: suspend DatabaseRepository.() -> T): T {
        firestore.enableNetwork().await()

        val result = transaction(this)

        firestore.disableNetwork().await()

        return result
    }

    override suspend fun <T> localTransaction(transaction: suspend DatabaseRepository.() -> T): T {
        firestore.disableNetwork().await()

        val result = transaction(this)

        firestore.enableNetwork().await()

        return result
    }
}