package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.ObserveQuartersUseCaseError
import kotlinx.coroutines.flow.Flow

class ObserveQuartersUseCase(
	private val quarterRepository: QuarterRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, List<Quarter>, ObserveQuartersUseCaseError>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: Unit): Flow<List<Quarter>> {
		return quarterRepository.observeQuartersFlow()
	}
}
