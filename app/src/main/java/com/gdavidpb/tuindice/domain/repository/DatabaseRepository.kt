package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstData
import com.gdavidpb.tuindice.domain.usecase.request.UpdateEvaluationRequest
import com.gdavidpb.tuindice.domain.usecase.request.UpdateSubjectRequest

interface DatabaseRepository {
    suspend fun getAccount(uid: String): Account
    suspend fun syncAccount(uid: String, data: Collection<DstData>)

    suspend fun getQuarters(uid: String): List<Quarter>
    suspend fun getCurrentQuarter(uid: String): Quarter?

    suspend fun getSubject(uid: String, sid: String): Subject
    suspend fun updateSubject(uid: String, request: UpdateSubjectRequest)
    suspend fun getQuarterSubjects(uid: String, qid: String): List<Subject>

    suspend fun getEvaluation(uid: String, eid: String): Evaluation
    suspend fun addEvaluation(uid: String, evaluation: Evaluation): Evaluation
    suspend fun updateEvaluation(uid: String, request: UpdateEvaluationRequest): Evaluation
    suspend fun removeEvaluation(uid: String, eid: String)
    suspend fun getSubjectEvaluations(uid: String, sid: String): List<Evaluation>

    suspend fun setToken(uid: String, token: String)
    suspend fun setAuthData(uid: String, data: DstAuth)

    suspend fun clearPersistence()
    suspend fun close()
}