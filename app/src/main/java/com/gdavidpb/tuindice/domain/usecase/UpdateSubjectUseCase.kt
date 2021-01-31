package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import com.gdavidpb.tuindice.domain.usecase.request.SubjectUpdateRequest

open class UpdateSubjectUseCase(
        private val authRepository: AuthRepository,
        private val databaseRepository: DatabaseRepository
) : CompletableUseCase<SubjectUpdateRequest, Any>() {
    override suspend fun executeOnBackground(params: SubjectUpdateRequest) {
        val activeUId = authRepository.getActiveAuth().uid

        databaseRepository.updateSubject(uid = activeUId, sid = params.id, grade = params.grade)
    }
}