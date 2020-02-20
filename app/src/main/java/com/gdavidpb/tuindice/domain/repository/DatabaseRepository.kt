package com.gdavidpb.tuindice.domain.repository

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstData

interface DatabaseRepository {
    suspend fun syncAccount(uid: String)

    suspend fun getAccount(uid: String): Account

    suspend fun getQuarters(uid: String): List<Quarter>
    suspend fun getCurrentQuarter(uid: String): Quarter?

    suspend fun getSubject(uid: String, id: String): Subject
    suspend fun getSubjects(uid: String): List<Subject>
    suspend fun updateSubject(uid: String, subject: Subject)
    suspend fun removeSubjects(uid: String, vararg ids: String)

    suspend fun addEvaluation(uid: String, evaluation: Evaluation): Evaluation
    suspend fun getEvaluations(uid: String): List<Evaluation>
    suspend fun getSubjectEvaluations(uid: String, sid: String): List<Evaluation>
    suspend fun updateEvaluation(uid: String, evaluation: Evaluation)
    suspend fun removeEvaluation(uid: String, id: String)

    suspend fun updateToken(uid: String, token: String)
    suspend fun updateAuthData(uid: String, data: DstAuth)
    suspend fun updateData(uid: String, data: Collection<DstData>)

    suspend fun close()
    suspend fun clearPersistence()
}