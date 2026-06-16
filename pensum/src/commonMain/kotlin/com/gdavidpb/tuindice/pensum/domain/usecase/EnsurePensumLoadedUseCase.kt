package com.gdavidpb.tuindice.pensum.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class EnsurePensumLoadedUseCase(
    private val pensumRepository: PensumRepository,
    override val reportingRepository: ReportingRepository,
    override val exceptionHandler: UpdatePensumExceptionHandler
) : FlowUseCase<Unit, EnsurePensumLoadedUseCase.Result, UpdatePensumUseCaseError>() {
    sealed interface Result {
        data object Cached : Result
        data object RefreshStarted : Result
        data object RefreshSucceeded : Result
    }

    override suspend fun executeOnBackground(params: Unit): Flow<Result> {
        return flow {
            if (pensumRepository.hasSelectedPensumResponse()) {
                emit(Result.Cached)
                return@flow
            }
            emit(Result.RefreshStarted)
            pensumRepository.refreshPensum()
            emit(Result.RefreshSucceeded)
        }
    }
}
