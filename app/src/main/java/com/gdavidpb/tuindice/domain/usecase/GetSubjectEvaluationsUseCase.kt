package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.SubjectEvaluations
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import kotlinx.coroutines.Dispatchers

open class GetSubjectEvaluationsUseCase(
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<String, SubjectEvaluations>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: String): SubjectEvaluations? {
        return databaseRepository.localTransaction {
            val subject = getSubject(id = params)
            val evaluations = getSubjectEvaluations(sid = params)

            SubjectEvaluations(subject, evaluations)
        }
    }
}