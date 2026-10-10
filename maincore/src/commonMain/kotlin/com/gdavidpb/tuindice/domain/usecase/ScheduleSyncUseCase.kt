package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.domain.repository.CoreCacheStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class ScheduleSyncUseCase(
	private val sessionRepository: SessionRepository,
	private val credentialsRepository: CredentialsRepository,
	private val syncRepository: SyncRepository,
	private val syncStatusRepository: SyncStatusRepository,
	private val coreCacheStateRepository: CoreCacheStateRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		if (!sessionRepository.hasActiveSession()) return emptyFlow()
		if (!credentialsRepository.hasPassword()) {
			// A signed-in user without a readable password would otherwise skip every sync in
			// silence, forever. Latch it so the app asks for the password again; an
			// OutdatedCredentials latch already asks, so it is kept as is.
			if (!syncStatusRepository.getSyncStatus().requiresPassword) {
				syncStatusRepository.setSyncStatus(SyncStatus.MissingCredentials)
			}

			return emptyFlow()
		}

		syncRepository.scheduleSync(
			password = credentialsRepository.getPassword(),
			policy = if (coreCacheStateRepository.requiresBaseRehydration())
				SyncPolicy.ForceRefresh
			else
				SyncPolicy.RespectCooldown
		)

		return flowOf(Unit)
	}
}
