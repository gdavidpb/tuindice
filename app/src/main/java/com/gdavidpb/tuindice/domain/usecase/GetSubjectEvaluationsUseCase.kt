package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.SubjectEvaluations
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase

open class GetSubjectEvaluationsUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<String, SubjectEvaluations, Any>() {
    override suspend fun executeOnBackground(params: String): SubjectEvaluations? {
        val activeUId = authRepository.getActiveAuth().uid

        return with(databaseRepository) {
            val subject = getSubject(uid = activeUId, sid = params)
            val evaluations = getSubjectEvaluations(uid = activeUId, sid = params)

            SubjectEvaluations(subject, evaluations)
        }
    }
}