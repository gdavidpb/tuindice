package com.gdavidpb.tuindice.pensum.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.pensum.domain.repository.PensumSettingsRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.error.PensumSettingsUseCaseError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SetPensumSummaryCollapsedUseCase(
	private val pensumSettingsRepository: PensumSettingsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Boolean, Unit, PensumSettingsUseCaseError>() {
	override suspend fun executeOnBackground(params: Boolean): Flow<Unit> {
		return flow {
			pensumSettingsRepository.setSummaryCollapsed(params)
			emit(Unit)
		}
	}
}
