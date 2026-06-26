package com.gdavidpb.tuindice.pensum.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.pensum.domain.repository.PensumSettingsRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.error.PensumSettingsUseCaseError
import kotlinx.coroutines.flow.Flow

class ObservePensumSummaryCollapsedUseCase(
	private val pensumSettingsRepository: PensumSettingsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Boolean, PensumSettingsUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Boolean> {
		return pensumSettingsRepository.observeSummaryCollapsed()
	}

	fun currentValue(): Boolean {
		return pensumSettingsRepository.isSummaryCollapsed()
	}
}
