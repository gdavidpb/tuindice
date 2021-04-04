package com.gdavidpb.tuindice.datastores

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.domain.model.service.*
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
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

    override suspend fun syncAccount(uid: String, data: Collection<DstData>) {
        firestore
                .collection(UserCollection.COLLECTION)
                .document(uid)
                .get()
                .await()

        arrayOf(
                QuarterCollection.COLLECTION,
                SubjectCollection.COLLECTION,
                EvaluationCollection.COLLECTION
        ).forEach { collection ->
            firestore
                    .collection(collection)
                    .whereEqualTo(UserCollection.USER_ID, uid)
                    .get()
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

    override suspend fun getQuarter(uid: String, qid: String): Quarter {
        val quarterRef = firestore
                .collection(QuarterCollection.COLLECTION)
                .document(qid)

        return quarterRef
                .get()
                .await()
                .toQuarter(subjects = getQuarterSubjects(uid = uid, qid = qid))
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

    override suspend fun updateQuarter(uid: String, update: QuarterUpdate): Quarter {
        val quarterRef = firestore
                .collection(QuarterCollection.COLLECTION)
                .document(update.qid)

        val values = mapOf(
                QuarterCollection.GRADE to update.grade,
                QuarterCollection.GRADE_SUM to update.gradeSum
        )

        quarterRef.set(values, SetOptions.merge())

        return quarterRef
                .get()
                .await()
                .toQuarter(subjects = getQuarterSubjects(uid = uid, qid = update.qid))
    }

    override suspend fun removeQuarter(uid: String, qid: String) {
        firestore
                .collection(QuarterCollection.COLLECTION)
                .document(qid)
                .delete()
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

    override suspend fun updateSubject(uid: String, update: SubjectUpdate): Subject {
        val subjectRef = firestore
                .collection(SubjectCollection.COLLECTION)
                .document(update.sid)

        val subjectValues = mapOf(
                SubjectCollection.GRADE to update.grade,
                SubjectCollection.STATUS to update.grade.toSubjectStatus()
        )

        subjectRef.set(subjectValues, SetOptions.merge())

        return subjectRef
                .get()
                .await()
                .toSubject()
    }

    override suspend fun getEvaluation(uid: String, eid: String): Evaluation {
        return firestore
                .collection(EvaluationCollection.COLLECTION)
                .document(eid)
                .get()
                .await()
                .toEvaluation()
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

    override suspend fun updateEvaluation(uid: String, update: EvaluationUpdate): Evaluation {
        val evaluationRef = firestore
                .collection(EvaluationCollection.COLLECTION)
                .document(update.eid)

        val values = mapOf(
                EvaluationCollection.TYPE to update.type.ordinal,
                EvaluationCollection.GRADE to update.grade,
                EvaluationCollection.MAX_GRADE to update.maxGrade,
                EvaluationCollection.DATE to Timestamp(update.date),
                EvaluationCollection.NOTES to update.notes,
                EvaluationCollection.DONE to update.isDone
        )

        evaluationRef.set(values, SetOptions.merge())

        return evaluationRef
                .get()
                .await()
                .toEvaluation()
    }

    override suspend fun removeEvaluation(uid: String, eid: String) {
        firestore
                .collection(EvaluationCollection.COLLECTION)
                .document(eid)
                .delete()
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

    override suspend fun setToken(uid: String, token: String) {
        val userRef = firestore
                .collection(UserCollection.COLLECTION)
                .document(uid)

        val values = mapOf(
                UserCollection.TOKEN to token
        )

        userRef.set(values, SetOptions.merge())
    }

    override suspend fun setAuthData(uid: String, data: DstAuth) {
        val userRef = firestore
                .collection(UserCollection.COLLECTION)
                .document(uid)

        userRef.set(data, SetOptions.merge()).await()
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