package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

class ScheduleSyncUseCase(
	private val sessionRepository: SessionRepository,
	private val credentialsRepository: CredentialsRepository,
	private val syncRepository: SyncRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		if (!sessionRepository.hasActiveSession()) return emptyFlow()
		if (!credentialsRepository.hasPassword()) return emptyFlow()

		syncRepository.scheduleSync(
			password = credentialsRepository.getPassword()
		)

		return flowOf(Unit)
	}
}
