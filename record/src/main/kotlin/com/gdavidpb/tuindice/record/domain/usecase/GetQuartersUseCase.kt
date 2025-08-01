package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.GetQuartersExceptionHandler
import kotlinx.coroutines.flow.Flow

class GetQuartersUseCase(
	private val quarterRepository: QuarterRepository,
	override val exceptionHandler: GetQuartersExceptionHandler
) : FlowUseCase<Unit, List<Quarter>, GetQuartersUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<List<Quarter>> {
		return quarterRepository.getQuartersFlow()
	}
}