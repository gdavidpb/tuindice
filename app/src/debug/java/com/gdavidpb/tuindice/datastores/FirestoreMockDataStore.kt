package com.gdavidpb.tuindice.datastores

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.model.service.*
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.await
import com.gdavidpb.tuindice.utils.mappers.*
import com.google.firebase.firestore.*

open class FirestoreMockDataStore(
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

    override suspend fun getCurrentQuarter(uid: String): Quarter? {
        return firestore
                .collection(COLLECTION_QUARTER)
                .whereEqualTo(FIELD_QUARTER_USER_ID, uid)
                .whereEqualTo(FIELD_QUARTER_STATUS, STATUS_QUARTER_CURRENT)
                .limit(1)
                .get()
                .await()
                .map { it.toQuarter(subjects = getSubjects(uid = uid, qid = it.id)) }
                .firstOrNull()
    }

    override suspend fun getQuarters(uid: String): List<Quarter> {
        val quarters = firestore
                .collection(COLLECTION_QUARTER)
                .whereEqualTo(FIELD_DEFAULT_USER_ID, uid)
                .orderBy(FIELD_QUARTER_START_DATE, Query.Direction.DESCENDING)
                .get()
                .await()

        val subjects = firestore
                .collection(COLLECTION_SUBJECT)
                .whereEqualTo(FIELD_DEFAULT_USER_ID, uid)
                .get()
                .await()
                .map { it.toSubject() }
                .groupBy { it.qid }

        return quarters.map {
            val quarterSubjects = subjects[it.id] ?: listOf()

            it.toQuarter(quarterSubjects)
        }
    }

    override suspend fun getSubjectEvaluations(uid: String, sid: String): List<Evaluation> {
        return firestore
                .collection(COLLECTION_EVALUATION)
                .whereEqualTo(FIELD_EVALUATION_USER_ID, uid)
                .whereEqualTo(FIELD_EVALUATION_SUBJECT_ID, sid)
                .get()
                .await()
                .map { it.toEvaluation() }
    }

    override suspend fun getEvaluations(uid: String): List<Evaluation> {
        return firestore
                .collection(COLLECTION_EVALUATION)
                .whereEqualTo(FIELD_EVALUATION_USER_ID, uid)
                .get()
                .await()
                .map { it.toEvaluation() }
    }

    override suspend fun getSubject(uid: String, sid: String): Subject {
        return firestore
                .collection(COLLECTION_SUBJECT)
                .document(sid)
                .get()
                .await()
                .toSubject()
    }

    override suspend fun getSubjects(uid: String, qid: String): List<Subject> {
        return firestore
                .collection(COLLECTION_SUBJECT)
                .whereEqualTo(FIELD_SUBJECT_USER_ID, uid)
                .whereEqualTo(FIELD_SUBJECT_QUARTER_ID, qid)
                .get()
                .await()
                .map { it.toSubject() }
    }

    override suspend fun updateEvaluation(uid: String, evaluation: Evaluation) {
        val evaluationRef = firestore
                .collection(COLLECTION_EVALUATION)
                .document(evaluation.id)

        val entity = evaluation.toEvaluationEntity(uid)

        evaluationRef.set(entity, SetOptions.merge())
    }

    override suspend fun removeEvaluation(uid: String, id: String) {
        firestore
                .collection(COLLECTION_EVALUATION)
                .document(id)
                .delete()
                .await()
    }

    override suspend fun addEvaluation(uid: String, evaluation: Evaluation): Evaluation {
        val entity = evaluation.toEvaluationEntity(uid)

        return firestore
                .collection(COLLECTION_EVALUATION)
                .document()
                .let { document ->
                    document.set(entity)

                    evaluation.copy(id = document.id)
                }
    }

    override suspend fun updateSubject(uid: String, sid: String, grade: Int) {
        val subjectRef = firestore
                .collection(COLLECTION_SUBJECT)
                .document(sid)

        val status = if (grade != 0) STATUS_SUBJECT_OK else STATUS_SUBJECT_RETIRED

        val values = mapOf(
                FIELD_SUBJECT_GRADE to grade,
                FIELD_SUBJECT_STATUS to status
        )

        subjectRef.set(values, SetOptions.merge()).await()
    }

    override suspend fun removeSubjects(uid: String, vararg sid: String) {
        if (sid.isEmpty()) return

        val batch = firestore.batch()

        sid.forEach { subjectId ->
            val subjectRef = firestore
                    .collection(COLLECTION_SUBJECT)
                    .document(subjectId)

            batch.delete(subjectRef)
        }

        batch.commit().await()
    }

    override suspend fun setAuthData(uid: String, data: DstAuth) {
        val userRef = firestore
                .collection(COLLECTION_USER)
                .document(uid)

        userRef.set(data, SetOptions.merge()).await()
    }

    override suspend fun syncAccount(uid: String, data: Collection<DstData>) {
        firestore
                .collection(COLLECTION_USER)
                .document(uid)
                .get(Source.SERVER)
                .await()

        arrayOf(
                COLLECTION_QUARTER,
                COLLECTION_SUBJECT,
                COLLECTION_EVALUATION
        ).forEach { collection ->
            firestore
                    .collection(collection)
                    .whereEqualTo(FIELD_DEFAULT_USER_ID, uid)
                    .get(Source.SERVER)
                    .await()
        }

        val batch = firestore.batch()

        batch.updateLastUpdate(uid)

        data.forEach { entry ->
            when (entry) {
                is DstPersonal -> batch.updatePersonalData(uid, entry)
                is DstRecord -> batch.updateRecordData(uid, entry)
                is DstEnrollment -> batch.updateEnrollmentData(uid, entry)
            }
        }

        batch.commit().await()
    }

    override suspend fun setToken(uid: String, token: String) {
        val userRef = firestore
                .collection(COLLECTION_USER)
                .document(uid)

        val values = mapOf(
                FIELD_USER_TOKEN to token
        )

        userRef.set(values, SetOptions.merge())
    }

    override suspend fun clearPersistence() {
        firestore.clearPersistence().await()
    }

    override suspend fun close() {
        firestore.terminate().await()
    }

    private fun WriteBatch.updateLastUpdate(uid: String) {
        val userRef = firestore
                .collection(COLLECTION_USER)
                .document(uid)

        val values = mapOf(
                FIELD_USER_LAST_UPDATE to FieldValue.serverTimestamp(),
                FIELD_USER_APP_VERSION_CODE to BuildConfig.VERSION_CODE
        )

        set(userRef, values, SetOptions.merge())
    }

    private fun WriteBatch.updatePersonalData(uid: String, data: DstPersonal) {
        val userRef = firestore
                .collection(COLLECTION_USER)
                .document(uid)

        set(userRef, data, SetOptions.merge())
    }

    private fun WriteBatch.updateRecordData(uid: String, data: DstRecord) {
        val userRef = firestore
                .collection(COLLECTION_USER)
                .document(uid)

        set(userRef, data.stats, SetOptions.merge())

        data.quarters.forEach { dstQuarter ->
            val quarter = dstQuarter.toQuarterEntity(uid)

            val quarterId = quarter.generateId()

            val quarterRef = firestore
                    .collection(COLLECTION_QUARTER)
                    .document(quarterId)

            set(quarterRef, quarter, SetOptions.merge())

            dstQuarter.subjects.forEach { dstSubject ->
                val subject = dstSubject.toSubjectEntity(uid = uid, qid = quarterId)

                val subjectId = subject.generateId()

                val subjectRef = firestore
                        .collection(COLLECTION_SUBJECT)
                        .document(subjectId)

                set(subjectRef, subject, SetOptions.merge())
            }
        }
    }

    private fun WriteBatch.updateEnrollmentData(uid: String, data: DstEnrollment) {
        val quarter = data.toQuarterEntity(uid)

        val quarterId = quarter.generateId()

        val quarterRef = firestore
                .collection(COLLECTION_QUARTER)
                .document(quarterId)

        set(quarterRef, quarter, SetOptions.merge())

        data.schedule.forEach { scheduleSubject ->
            val subject = scheduleSubject.toSubjectEntity(uid = uid, qid = quarterId)

            val subjectId = subject.generateId()

            val subjectRef = firestore
                    .collection(COLLECTION_SUBJECT)
                    .document(subjectId)

            val currentSubject = subject.toCurrentSubjectEntity()

            set(subjectRef, currentSubject, SetOptions.merge())
        }
    }
}