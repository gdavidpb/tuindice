package com.gdavidpb.tuindice.pensum.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.error.ObservePensumUseCaseError
import kotlinx.coroutines.flow.Flow

class ObservePensumUseCase(
	private val pensumRepository: PensumRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, PensumObservation, ObservePensumUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<PensumObservation> {
		return pensumRepository.observePensumFlow()
	}
}
