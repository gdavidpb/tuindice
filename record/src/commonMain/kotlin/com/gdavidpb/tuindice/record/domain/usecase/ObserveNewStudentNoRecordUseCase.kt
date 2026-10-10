package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Whether the university has no record for this account yet. It is observed apart from the record
 * because such an account has no record stored, and the record observation never emits without one:
 * read from there, the screen would never learn why loading it failed.
 */
class ObserveNewStudentNoRecordUseCase(
	private val syncStatusRepository: SyncStatusRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Boolean, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Boolean> {
		return syncStatusRepository.observeSyncStatus()
			.map { syncStatus -> syncStatus == SyncStatus.NewStudentNoRecord }
			.distinctUntilChanged()
	}
}
