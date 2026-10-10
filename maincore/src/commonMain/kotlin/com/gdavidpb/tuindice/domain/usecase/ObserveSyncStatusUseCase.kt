package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow

/**
 * How the last sync ended, followed as it changes. The app host needs it for what no single
 * screen owns: asking for the password again when the stored one stopped working.
 */
class ObserveSyncStatusUseCase(
	private val syncStatusRepository: SyncStatusRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, SyncStatus, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<SyncStatus> {
		return syncStatusRepository.observeSyncStatus()
	}
}
