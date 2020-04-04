package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.SubjectEvaluations
import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers

@IgnoredExceptions(CancellationException::class)
open class GetSubjectEvaluationsUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : ResultUseCase<String, SubjectEvaluations>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: String): SubjectEvaluations? {
        val activeUId = authRepository.getActiveAuth().uid

        return with(databaseRepository) {
            val subject = getSubject(uid = activeUId, sid = params)
            val evaluations = getSubjectEvaluations(uid = activeUId, sid = params)

            SubjectEvaluations(subject, evaluations)
        }
    }
}