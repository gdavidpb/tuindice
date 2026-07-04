package com.gdavidpb.tuindice.pensum.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.repository.PensumSelectionRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.error.ObservePensumUseCaseError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObservePensumUseCase(
	private val pensumRepository: PensumRepository,
	private val pensumSelectionRepository: PensumSelectionRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, PensumObservation, ObservePensumUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<PensumObservation> {
		return combine(
			pensumRepository.observePensumFlow(),
			pensumSelectionRepository.observeSummaryCollapsed()
		) { observation, isSummaryCollapsed ->
			when (observation) {
				is PensumObservation.Content -> observation.copy(
					isSummaryCollapsed = isSummaryCollapsed
				)

				else -> observation
			}
		}
	}
}
