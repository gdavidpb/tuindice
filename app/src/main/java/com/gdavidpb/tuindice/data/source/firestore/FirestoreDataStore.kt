package com.gdavidpb.tuindice.data.source.firestore

import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import com.gdavidpb.tuindice.domain.model.service.DstRecord
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

open class FirestoreDataStore(
        private val auth: FirebaseAuth,
        private val firestore: FirebaseFirestore
) : DatabaseRepository {
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
}