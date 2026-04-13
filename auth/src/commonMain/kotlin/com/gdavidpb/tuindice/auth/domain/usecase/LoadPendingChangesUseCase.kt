package com.gdavidpb.tuindice.auth.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.repository.PendingChangesRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LoadPendingChangesUseCase(
	private val pendingChangesRepository: PendingChangesRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, PendingChanges, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: Unit): Flow<PendingChanges> {
		return flowOf(
			pendingChangesRepository.getPendingChanges()
		)
	}
}
