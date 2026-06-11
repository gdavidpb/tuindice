package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.SyncPolicy
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.domain.repository.CoreCacheStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class ScheduleSyncUseCase(
	private val sessionRepository: SessionRepository,
	private val credentialsRepository: CredentialsRepository,
	private val syncRepository: SyncRepository,
	private val coreCacheStateRepository: CoreCacheStateRepository,
	override val reportingRepository: ReportingRepository,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : FlowUseCase<Unit, Unit, Nothing>(
	reportingRepository = reportingRepository,
	dispatchers = dispatchers
) {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		if (!sessionRepository.hasActiveSession()) return emptyFlow()
		if (!credentialsRepository.hasPassword()) return emptyFlow()

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
