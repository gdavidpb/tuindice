package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.UpdateQuartersUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.UpdateQuartersExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateQuartersUseCase(
	private val quarterRepository: QuarterRepository,
	override val exceptionHandler: UpdateQuartersExceptionHandler
) : FlowUseCase<Unit, Unit, UpdateQuartersUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		quarterRepository.updateQuarters()

		return flowOf(Unit)
	}
}
