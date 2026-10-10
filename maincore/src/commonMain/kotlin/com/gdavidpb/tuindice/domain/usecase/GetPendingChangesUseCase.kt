package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.repository.PendingChangesRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * What the device still has to send to the server. Read before offering to sign out, so the
 * dialog can say what would be lost; signing out itself is another use case.
 */
class GetPendingChangesUseCase(
	private val pendingChangesRepository: PendingChangesRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, PendingChanges, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<PendingChanges> {
		return flowOf(pendingChangesRepository.getPendingChanges())
	}
}
