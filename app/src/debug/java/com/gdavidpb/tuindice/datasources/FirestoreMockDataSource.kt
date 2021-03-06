package com.gdavidpb.tuindice.datasources

import com.gdavidpb.tuindice.data.model.database.EvaluationUpdate
import com.gdavidpb.tuindice.data.model.database.QuarterUpdate
import com.gdavidpb.tuindice.data.model.database.SubjectUpdate
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.isUpdated
import com.gdavidpb.tuindice.utils.mappers.*
import com.google.firebase.firestore.*
import kotlinx.coroutines.tasks.await
import java.util.concurrent.atomic.AtomicReference

open class FirestoreMockDataSource(
        private val firestore: FirebaseFirestore
) : DatabaseRepository {
    private val atomicBatch = AtomicReference<WriteBatch>(null)

    private object MergeOptions {
        val mergeAll = SetOptions.merge()

        val noMergeQuarter = SetOptions.mergeFields(
                QuarterCollection.USER_ID,
                QuarterCollection.START_DATE,
                QuarterCollection.END_DATE,
                QuarterCollection.STATUS
        )

        val noMergeSubject = SetOptions.mergeFields(
                SubjectCollection.USER_ID,
                SubjectCollection.QUARTER_ID,
                SubjectCollection.CODE,
                SubjectCollection.NAME,
                SubjectCollection.CREDITS
        )
    }

    override suspend fun isUpdated(uid: String): Boolean {
        return firestore
                .collection(UserCollection.COLLECTION)
                .document(uid)
                .get()
                .await()
                .getTimestamp(UserCollection.LAST_UPDATE)
                ?.toDate()
                ?.isUpdated()
                ?: false
    }

    override suspend fun addAccount(uid: String, account: Account) {
        val entity = account.toAccountEntity()

        firestore
                .collection(UserCollection.COLLECTION)
                .document(uid)
                .also { document -> set(document, entity) }
    }

    override suspend fun getAccount(uid: String): Account {
        return firestore
                .collection(UserCollection.COLLECTION)
                .document(uid)
                .get()
                .await()
                .toAccount()
    }

    override suspend fun addQuarter(uid: String, quarter: Quarter): Quarter {
        val quarterEntity = quarter.toQuarterEntity(uid)
        val (quarterSetOptions, subjectSetOptions) = computeQuarterMerges(quarter)

        firestore
                .collection(QuarterCollection.COLLECTION)
                .document(quarter.id)
                .also { document -> set(document, quarterEntity, quarterSetOptions) }

        quarter.subjects.forEach { subject ->
            val subjectEntity = subject.toSubjectEntity(uid = uid)

            firestore
                    .collection(SubjectCollection.COLLECTION)
                    .document(subject.id)
                    .also { document -> set(document, subjectEntity, subjectSetOptions) }
        }

        return quarter
    }

    override suspend fun getQuarter(uid: String, qid: String): Quarter {
        return firestore
                .collection(QuarterCollection.COLLECTION)
                .document(qid)
                .get()
                .await()
                .toQuarter(subjects = getQuarterSubjects(uid = uid, qid = qid))
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

    override suspend fun updateQuarter(uid: String, qid: String, update: QuarterUpdate): Quarter {
        return firestore
                .collection(QuarterCollection.COLLECTION)
                .document(qid)
                .also { document -> set(document, update) }
                .get()
                .await()
                .toQuarter(subjects = getQuarterSubjects(uid = uid, qid = qid))
    }

    override suspend fun removeQuarter(uid: String, qid: String) {
        firestore
                .collection(QuarterCollection.COLLECTION)
                .document(qid)
                .also { document -> delete(document) }
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

    override suspend fun updateSubject(uid: String, sid: String, update: SubjectUpdate): Subject {
        return firestore
                .collection(SubjectCollection.COLLECTION)
                .document(sid)
                .also { document -> set(document, update) }
                .get()
                .await()
                .toSubject()
    }

    override suspend fun addEvaluation(uid: String, evaluation: Evaluation): Evaluation {
        val entity = evaluation.toEvaluationEntity(uid)

        return firestore
                .collection(EvaluationCollection.COLLECTION)
                .document()
                .let { document ->
                    set(document, entity)

                    evaluation.copy(id = document.id)
                }
    }

    override suspend fun getEvaluation(uid: String, eid: String): Evaluation {
        return firestore
                .collection(EvaluationCollection.COLLECTION)
                .document(eid)
                .get()
                .await()
                .toEvaluation()
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

    override suspend fun updateEvaluation(uid: String, eid: String, update: EvaluationUpdate): Evaluation {
        return firestore
                .collection(EvaluationCollection.COLLECTION)
                .document(eid)
                .also { document -> set(document, update) }
                .get()
                .await()
                .toEvaluation()
    }

    override suspend fun removeEvaluation(uid: String, eid: String) {
        firestore
                .collection(EvaluationCollection.COLLECTION)
                .document(eid)
                .also { document -> delete(document) }
    }

    override suspend fun updateToken(uid: String, token: String) {
        firestore
                .collection(UserCollection.COLLECTION)
                .document(uid)
                .also { document -> set(document, mapOf(UserCollection.TOKEN to token)) }
    }

    override suspend fun runBatch(batch: suspend DatabaseRepository.() -> Unit) {
        val isReady = atomicBatch.compareAndSet(null, firestore.batch())

        if (isReady) {
            batch()
            atomicBatch.getAndSet(null).commit().await()
        }
    }

    override suspend fun hasCache(uid: String): Boolean {
        return firestore
                .collection(UserCollection.COLLECTION)
                .document(uid)
                .get()
                .await()
                .exists()
    }

    override suspend fun cache(uid: String) {
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
    }

    override suspend fun clearCache() {
        firestore.clearPersistence().await()
    }

    override suspend fun close() {
        firestore.terminate().await()
    }

    private fun set(documentRef: DocumentReference, data: Any, options: SetOptions = SetOptions.merge()) {
        atomicBatch.get()
                ?.apply { set(documentRef, data, options) }
                ?: documentRef.set(data, options)
    }

    private fun delete(documentRef: DocumentReference) {
        atomicBatch.get()
                ?.apply { delete(documentRef) }
                ?: documentRef.delete()
    }

    private suspend fun computeQuarterMerges(quarter: Quarter): Pair<SetOptions, SetOptions> {
        val isFinished = (quarter.status == STATUS_QUARTER_COMPLETED) || (quarter.status == STATUS_QUARTER_RETIRED)

        return if (isFinished) {
            MergeOptions.mergeAll to MergeOptions.mergeAll
        } else {
            val quarterSnapshot = firestore
                    .collection(QuarterCollection.COLLECTION)
                    .document(quarter.id)
                    .get()
                    .await()

            val quarterExists = quarterSnapshot.exists()

            val quarterSetOptions = if (quarterExists) MergeOptions.noMergeQuarter else MergeOptions.mergeAll
            val subjectSetOptions = if (quarterExists) MergeOptions.noMergeSubject else MergeOptions.mergeAll

            quarterSetOptions to subjectSetOptions
        }
    }
}