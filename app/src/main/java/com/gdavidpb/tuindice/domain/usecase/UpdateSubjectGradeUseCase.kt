package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import com.gdavidpb.tuindice.domain.usecase.request.SubjectUpdateRequest
import kotlinx.coroutines.Dispatchers

open class UpdateSubjectGradeUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : CompletableUseCase<SubjectUpdateRequest>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: SubjectUpdateRequest) {
        val activeUId = authRepository.getActiveAuth().uid

        databaseRepository.updateSubjectGrade(uid = activeUId, sid = params.id, grade = params.grade)
    }
}