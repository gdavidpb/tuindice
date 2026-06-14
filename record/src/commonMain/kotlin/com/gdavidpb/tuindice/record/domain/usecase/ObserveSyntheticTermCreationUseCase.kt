package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationSnapshot
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermCreationRepository
import com.gdavidpb.tuindice.record.domain.usecase.param.ObserveSyntheticTermCreationParams
import kotlinx.coroutines.flow.Flow

class ObserveSyntheticTermCreationUseCase(
	private val repository: SyntheticTermCreationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<ObserveSyntheticTermCreationParams, SyntheticTermCreationSnapshot, Nothing>() {
	override suspend fun executeOnBackground(
		params: ObserveSyntheticTermCreationParams
	): Flow<SyntheticTermCreationSnapshot> {
		return repository.observeSnapshot(
			queryFlow = params.queryFlow,
			selectedSubjectsFlow = params.selectedSubjectsFlow,
			selectedPeriodKeyFlow = params.selectedPeriodKeyFlow,
			editingTermIdFlow = params.editingTermIdFlow,
			editingTermKeyFlow = params.editingTermKeyFlow
		)
	}
}
