package com.gdavidpb.tuindice.pensum.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import com.gdavidpb.tuindice.pensum.domain.usecase.param.SelectPensumParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SelectPensumUseCase(
	private val pensumRepository: PensumRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: UpdatePensumExceptionHandler
) : FlowUseCase<SelectPensumParams, Unit, UpdatePensumUseCaseError>() {
	override suspend fun executeOnBackground(params: SelectPensumParams): Flow<Unit> {
		return flow {
			pensumRepository.selectPensum(
				year = params.year
			)
			emit(Unit)
		}
	}
}
