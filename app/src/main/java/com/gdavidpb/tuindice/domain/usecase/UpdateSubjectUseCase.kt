package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import kotlinx.coroutines.Dispatchers

open class UpdateSubjectUseCase(
        private val databaseRepository: DatabaseRepository
) : CompletableUseCase<Subject>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Subject) {
        databaseRepository.localTransaction {
            updateSubject(subject = params)
        }
    }
}