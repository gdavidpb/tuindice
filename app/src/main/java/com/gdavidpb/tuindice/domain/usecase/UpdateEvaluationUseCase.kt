package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.domain.usecase.coroutines.CompletableUseCase
import kotlinx.coroutines.Dispatchers

open class UpdateEvaluationUseCase(
        private val databaseRepository: DatabaseRepository
) : CompletableUseCase<Evaluation>(
        backgroundContext = Dispatchers.IO,
        foregroundContext = Dispatchers.Main
) {
    override suspend fun executeOnBackground(params: Evaluation) {
        databaseRepository.localTransaction {
            updateEvaluation(evaluation = params)
        }
    }
}