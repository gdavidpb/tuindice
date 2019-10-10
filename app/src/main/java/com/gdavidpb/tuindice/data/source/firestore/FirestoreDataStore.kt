package com.gdavidpb.tuindice.data.source.firestore

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.model.service.*
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.utils.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

open class FirestoreDataStore(
        private val firestore: FirebaseFirestore
) : DatabaseRepository {
    override suspend fun getAccount(uid: String): Account {
        return firestore
                .collection(COLLECTION_USER)
                .document(uid)
                .get()
                .await()
                .toAccount()
    }

    override suspend fun getQuarters(uid: String): List<Quarter> {
        suspend fun getSubjectsMap(): Map<String, List<Subject>> {
            return firestore
                    .collection(COLLECTION_SUBJECT)
                    .whereEqualTo(FIELD_SUBJECT_USER_ID, uid)
                    .get()
                    .await()
                    .documents
                    .map { it.toSubject() }
                    .groupBy { it.qid }
        }

        val subjectsMap = getSubjectsMap()

        return firestore
                .collection(COLLECTION_QUARTER)
                .whereEqualTo(FIELD_QUARTER_USER_ID, uid)
                .orderBy(FIELD_QUARTER_START_DATE, Query.Direction.DESCENDING)
                .get()
                .await()
                .documents
                .map {
                    val subjects = subjectsMap[it.id] ?: listOf()

                    it.toQuarter(subjects)
                }
    }

    override suspend fun updateSubject(subject: Subject) {
        val subjectRef = firestore
                .collection(COLLECTION_SUBJECT)
                .document(subject.id)

        val values = mapOf(
                FIELD_SUBJECT_STATUS to subject.status,
                FIELD_SUBJECT_GRADE to subject.grade
        )

        subjectRef.set(values, SetOptions.merge()).await()
    }

    override suspend fun updateAuthData(uid: String, data: DstAuth) {
        val userRef = firestore
                .collection(COLLECTION_USER)
                .document(uid)

        userRef.set(data, SetOptions.merge()).await()
    }

    override suspend fun updateData(uid: String, data: Collection<DstData>) {
        val batch = firestore.batch()

        data.forEach { entry ->
            when (entry) {
                is DstPersonal -> {
                    val userRef = firestore
                            .collection(COLLECTION_USER)
                            .document(uid)

                    batch.set(userRef, entry, SetOptions.merge())
                }
                is DstRecord -> {
                    val userRef = firestore
                            .collection(COLLECTION_USER)
                            .document(uid)

                    batch.set(userRef, entry.stats, SetOptions.merge())

                    entry.quarters.forEach { dstQuarter ->
                        val quarter = dstQuarter.toQuarterEntity(uid)

                        val quarterId = quarter.generateId()

                        val quarterRef = firestore
                                .collection(COLLECTION_QUARTER)
                                .document(quarterId)

                        batch.set(quarterRef, quarter, SetOptions.merge())

                        dstQuarter.subjects.forEach { dstSubject ->
                            val subject = dstSubject.toSubjectEntity(uid = uid, qid = quarterId)

                            val subjectId = subject.generateId()

                            val subjectRef = firestore
                                    .collection(COLLECTION_SUBJECT)
                                    .document(subjectId)

                            batch.set(subjectRef, subject, SetOptions.merge())
                        }
                    }
                }
                is DstEnrollment -> {
                    val quarter = entry.toQuarterEntity(uid)

                    val quarterId = quarter.generateId()

                    val quarterRef = firestore
                            .collection(COLLECTION_QUARTER)
                            .document(quarterId)

                    batch.set(quarterRef, quarter, SetOptions.merge())

                    entry.schedule.forEach { scheduleSubject ->
                        val subject = scheduleSubject.toSubjectEntity(uid = uid, qid = quarterId)

                        val subjectId = subject.generateId()

                        val subjectRef = firestore
                                .collection(COLLECTION_SUBJECT)
                                .document(subjectId)

                        val noGrade = subject.toNoGrade()

                        batch.set(subjectRef, noGrade, SetOptions.merge())
                    }
                }
            }
        }

        batch.commit().await()
    }

    override suspend fun updateToken(uid: String, token: String) {
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