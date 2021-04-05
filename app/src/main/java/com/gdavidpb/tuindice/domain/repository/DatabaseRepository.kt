package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.data.model.database.EvaluationUpdate
import com.gdavidpb.tuindice.data.model.database.QuarterUpdate
import com.gdavidpb.tuindice.data.model.database.SubjectUpdate
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstData

interface DatabaseRepository {
    suspend fun getAccount(uid: String): Account
    suspend fun syncAccount(uid: String, data: Collection<DstData>)

    suspend fun getQuarters(uid: String): List<Quarter>
    suspend fun getQuarter(uid: String, qid: String): Quarter
    suspend fun getCurrentQuarter(uid: String): Quarter?
    suspend fun updateQuarter(uid: String, qid: String, update: QuarterUpdate): Quarter
    suspend fun removeQuarter(uid: String, qid: String)

    suspend fun getSubject(uid: String, sid: String): Subject
    suspend fun getQuarterSubjects(uid: String, qid: String): List<Subject>
    suspend fun updateSubject(uid: String, sid: String, update: SubjectUpdate): Subject

    suspend fun getEvaluation(uid: String, eid: String): Evaluation
    suspend fun addEvaluation(uid: String, evaluation: Evaluation): Evaluation
    suspend fun updateEvaluation(uid: String, eid: String, update: EvaluationUpdate): Evaluation
    suspend fun removeEvaluation(uid: String, eid: String)
    suspend fun getSubjectEvaluations(uid: String, sid: String): List<Evaluation>

    suspend fun setToken(uid: String, token: String)
    suspend fun setAuthData(uid: String, data: DstAuth)

    suspend fun clearPersistence()
    suspend fun close()
}