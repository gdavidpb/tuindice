package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import com.gdavidpb.tuindice.domain.usecase.request.SubjectUpdateRequest
import com.gdavidpb.tuindice.utils.annotations.IgnoredExceptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers

@IgnoredExceptions(CancellationException::class)
open class UpdateSubjectUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : CompletableUseCase<SubjectUpdateRequest>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: SubjectUpdateRequest) {
        val activeUId = authRepository.getActiveAuth().uid

        databaseRepository.updateSubject(uid = activeUId, sid = params.id, grade = params.grade)
    }
}