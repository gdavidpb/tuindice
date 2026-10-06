package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.model.ObservedSync
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * What the sync says of the account. It is observed apart from the user because an account with no
 * user stored never emits one, and the screen that failed to load it still has to tell why.
 */
class ObserveSyncUseCase(
	private val syncStatusRepository: SyncStatusRepository,
	private val syncRepository: SyncRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, ObservedSync, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<ObservedSync> {
		return combine(
			syncStatusRepository.observeSyncStatus(),
			syncStatusRepository.observeSyncReport(),
			syncStatusRepository.observeLastSuccessfulSyncAt(),
			syncRepository.observeSyncInProgress()
		) { status, report, lastSuccessfulSyncAt, isInProgress ->
			ObservedSync(
				status = status,
				report = report,
				lastSuccessfulSyncAt = lastSuccessfulSyncAt,
				isInProgress = isInProgress
			)
		}
	}
}
