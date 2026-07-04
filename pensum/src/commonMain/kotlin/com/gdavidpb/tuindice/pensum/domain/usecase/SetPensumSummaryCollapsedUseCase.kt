package com.gdavidpb.tuindice.pensum.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.pensum.domain.repository.PensumSelectionRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.error.PensumSettingsUseCaseError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SetPensumSummaryCollapsedUseCase(
	private val pensumSelectionRepository: PensumSelectionRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Boolean, Unit, PensumSettingsUseCaseError>() {
	override suspend fun executeOnBackground(params: Boolean): Flow<Unit> {
		return flow {
			pensumSelectionRepository.setSummaryCollapsed(params)
			emit(Unit)
		}
	}
}
