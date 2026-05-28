package com.gdavidpb.tuindice.pensum.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UpdatePensumUseCase(
	private val pensumRepository: PensumRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: UpdatePensumExceptionHandler
) : FlowUseCase<Unit, Unit, UpdatePensumUseCaseError>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		return flow {
			pensumRepository.refreshPensum()
			emit(Unit)
		}
	}
}
