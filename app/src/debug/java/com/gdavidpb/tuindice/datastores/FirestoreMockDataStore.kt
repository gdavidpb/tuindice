package com.gdavidpb.tuindice.datastores

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.model.service.*
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.domain.usecase.request.UpdateSubjectRequest
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.toSubjectStatus
import com.gdavidpb.tuindice.utils.mappers.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await

open class FirestoreMockDataStore(
        private val firestore: FirebaseFirestore
) : DatabaseRepository {
    override suspend fun getAccount(uid: String): Account {
        return firestore
                .collection(UserCollection.COLLECTION)
                .document(uid)
                .get()
                .await()
                .toAccount()
    }

    override suspend fun getCurrentQuarter(uid: String): Quarter? {
        return firestore
                .collection(QuarterCollection.COLLECTION)
                .whereEqualTo(QuarterCollection.USER_ID, uid)
                .whereEqualTo(QuarterCollection.STATUS, STATUS_QUARTER_CURRENT)
                .limit(1)
                .get()
                .await()
                .map { it.toQuarter(subjects = getQuarterSubjects(uid = uid, qid = it.id)) }
                .firstOrNull()
    }

    override suspend fun getQuarters(uid: String): List<Quarter> {
        val quarters = firestore
                .collection(QuarterCollection.COLLECTION)
                .whereEqualTo(QuarterCollection.USER_ID, uid)
                .orderBy(QuarterCollection.START_DATE, Query.Direction.DESCENDING)
                .get()
                .await()

        val subjects = firestore
                .collection(SubjectCollection.COLLECTION)
                .whereEqualTo(SubjectCollection.USER_ID, uid)
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
                .collection(EvaluationCollection.COLLECTION)
                .whereEqualTo(EvaluationCollection.USER_ID, uid)
                .whereEqualTo(EvaluationCollection.SUBJECT_ID, sid)
                .get()
                .await()
                .map { it.toEvaluation() }
    }

    override suspend fun getSubject(uid: String, sid: String): Subject {
        return firestore
                .collection(SubjectCollection.COLLECTION)
                .document(sid)
                .get()
                .await()
                .toSubject()
    }

    override suspend fun getQuarterSubjects(uid: String, qid: String): List<Subject> {
        return firestore
                .collection(SubjectCollection.COLLECTION)
                .whereEqualTo(SubjectCollection.USER_ID, uid)
                .whereEqualTo(SubjectCollection.QUARTER_ID, qid)
                .get()
                .await()
                .map { it.toSubject() }
    }

    override suspend fun getEvaluation(uid: String, eid: String): Evaluation {
        return firestore
                .collection(EvaluationCollection.COLLECTION)
                .document(eid)
                .get()
                .await()
                .toEvaluation()
    }

    override suspend fun updateEvaluation(uid: String, request: UpdateEvaluationRequest) {
        val evaluationRef = firestore
                .collection(EvaluationCollection.COLLECTION)
                .document(request.id)

        val values = mapOf(
                EvaluationCollection.TYPE to request.type.ordinal,
                EvaluationCollection.GRADE to request.grade,
                EvaluationCollection.MAX_GRADE to request.maxGrade,
                EvaluationCollection.DATE to Timestamp(request.date),
                EvaluationCollection.NOTES to request.notes,
                EvaluationCollection.DONE to request.isDone
        )

        evaluationRef.set(values, SetOptions.merge())
    }

    override suspend fun removeEvaluation(uid: String, eid: String) {
        firestore
                .collection(EvaluationCollection.COLLECTION)
                .document(eid)
                .delete()
    }

    override suspend fun addEvaluation(uid: String, evaluation: Evaluation): Evaluation {
        val entity = evaluation.toEvaluationEntity(uid)

        return firestore
                .collection(EvaluationCollection.COLLECTION)
                .document()
                .let { document ->
                    document.set(entity)

                    evaluation.copy(id = document.id)
                }
    }

    override suspend fun updateSubject(uid: String, request: UpdateSubjectRequest) {
        val subjectRef = firestore
                .collection(SubjectCollection.COLLECTION)
                .document(request.id)

        val values = mapOf(
                SubjectCollection.GRADE to request.grade,
                SubjectCollection.STATUS to request.grade.toSubjectStatus()
        )

        subjectRef.set(values, SetOptions.merge())
    }

    override suspend fun setAuthData(uid: String, data: DstAuth) {
        val userRef = firestore
                .collection(UserCollection.COLLECTION)
                .document(uid)

        userRef.set(data, SetOptions.merge()).await()
    }

    override suspend fun syncAccount(uid: String, data: Collection<DstData>) {
        firestore
                .collection(UserCollection.COLLECTION)
                .document(uid)
                .get(Source.SERVER)
                .await()

        arrayOf(
                QuarterCollection.COLLECTION,
                SubjectCollection.COLLECTION,
                EvaluationCollection.COLLECTION
        ).forEach { collection ->
            firestore
                    .collection(collection)
                    .whereEqualTo(UserCollection.USER_ID, uid)
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
                .collection(UserCollection.COLLECTION)
                .document(uid)

        val values = mapOf(
                UserCollection.TOKEN to token
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
                .collection(UserCollection.COLLECTION)
                .document(uid)

        val values = mapOf(
                UserCollection.LAST_UPDATE to FieldValue.serverTimestamp(),
                UserCollection.APP_VERSION_CODE to BuildConfig.VERSION_CODE
        )

        set(userRef, values, SetOptions.merge())
    }

    private fun WriteBatch.updatePersonalData(uid: String, data: DstPersonal) {
        val userRef = firestore
                .collection(UserCollection.COLLECTION)
                .document(uid)

        set(userRef, data, SetOptions.merge())
    }

    private fun WriteBatch.updateRecordData(uid: String, data: DstRecord) {
        val userRef = firestore
                .collection(UserCollection.COLLECTION)
                .document(uid)

        set(userRef, data.stats, SetOptions.merge())

        data.quarters.forEach { dstQuarter ->
            val quarter = dstQuarter.toQuarterEntity(uid)

            val quarterId = quarter.generateId()

            val quarterRef = firestore
                    .collection(QuarterCollection.COLLECTION)
                    .document(quarterId)

            set(quarterRef, quarter, SetOptions.merge())

            dstQuarter.subjects.forEach { dstSubject ->
                val subject = dstSubject.toSubjectEntity(uid = uid, qid = quarterId)

                val subjectId = subject.generateId()

                val subjectRef = firestore
                        .collection(SubjectCollection.COLLECTION)
                        .document(subjectId)

                set(subjectRef, subject, SetOptions.merge())
            }
        }
    }

    private fun WriteBatch.updateEnrollmentData(uid: String, data: DstEnrollment) {
        val quarter = data.toQuarterEntity(uid)

        val quarterId = quarter.generateId()

        val quarterRef = firestore
                .collection(QuarterCollection.COLLECTION)
                .document(quarterId)

        set(quarterRef, quarter, SetOptions.merge())

        data.schedule.forEach { scheduleSubject ->
            val subject = scheduleSubject.toSubjectEntity(uid = uid, qid = quarterId)

            val subjectId = subject.generateId()

            val subjectRef = firestore
                    .collection(SubjectCollection.COLLECTION)
                    .document(subjectId)

            val currentSubject = subject.toCurrentSubjectEntity()

            set(subjectRef, currentSubject, SetOptions.merge())
        }
    }
}