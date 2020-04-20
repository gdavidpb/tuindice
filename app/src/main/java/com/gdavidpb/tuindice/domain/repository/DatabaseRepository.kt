package com.gdavidpb.tuindice.domain.repository

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
    suspend fun getCurrentQuarter(uid: String): Quarter?

    suspend fun getSubject(uid: String, sid: String): Subject
    suspend fun getSubjects(uid: String, qid: String): List<Subject>
    suspend fun updateSubject(uid: String, sid: String, grade: Int)
    suspend fun removeSubjects(uid: String, vararg sid: String)

    suspend fun addEvaluation(uid: String, evaluation: Evaluation): Evaluation
    suspend fun getEvaluations(uid: String): List<Evaluation>
    suspend fun getSubjectEvaluations(uid: String, sid: String): List<Evaluation>
    suspend fun updateEvaluation(uid: String, evaluation: Evaluation)
    suspend fun removeEvaluation(uid: String, id: String)

    suspend fun setToken(uid: String, token: String)
    suspend fun setAuthData(uid: String, data: DstAuth)
    suspend fun setHasProfilePicture(uid: String, hasProfilePicture: Boolean)

    suspend fun clearPersistence()
    suspend fun close()
}