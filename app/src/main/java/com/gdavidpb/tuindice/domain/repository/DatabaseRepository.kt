package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.data.model.database.EvaluationUpdate
import com.gdavidpb.tuindice.data.model.database.QuarterUpdate
import com.gdavidpb.tuindice.data.model.database.SubjectUpdate
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject

interface DatabaseRepository {
    suspend fun isUpdated(uid: String): Boolean

    suspend fun addAccount(uid: String, account: Account)
    suspend fun getAccount(uid: String): Account

    suspend fun addQuarter(uid: String, quarter: Quarter): Quarter
    suspend fun getQuarter(uid: String, qid: String): Quarter
    suspend fun getQuarters(uid: String): List<Quarter>
    suspend fun getCurrentQuarter(uid: String): Quarter
    suspend fun updateQuarter(uid: String, qid: String, update: QuarterUpdate): Quarter
    suspend fun removeQuarter(uid: String, qid: String)

    suspend fun getSubject(uid: String, sid: String): Subject
    suspend fun getQuarterSubjects(uid: String, qid: String): List<Subject>
    suspend fun updateSubject(uid: String, sid: String, update: SubjectUpdate): Subject

    suspend fun addEvaluation(uid: String, evaluation: Evaluation): Evaluation
    suspend fun getEvaluation(uid: String, eid: String): Evaluation
    suspend fun getSubjectEvaluations(uid: String, sid: String): List<Evaluation>
    suspend fun updateEvaluation(uid: String, eid: String, update: EvaluationUpdate): Evaluation
    suspend fun removeEvaluation(uid: String, eid: String)

    suspend fun setToken(uid: String, token: String)

    suspend fun runBatch(batch: suspend DatabaseRepository.() -> Unit)
    suspend fun cache(uid: String)
    suspend fun clearCache()
    suspend fun close()
}