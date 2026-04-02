package com.gdavidpb.tuindice.auth.domain.usecase

import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignOutUseCase(
	private val authRepository: AuthRepository,
	private val sessionRepository: SessionRepository,
	private val messagingRepository: MessagingRepository,
	private val applicationRepository: ApplicationRepository,
	private val syncStatusRepository: SyncStatusRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		val accessToken = sessionRepository.getAccessToken()

		coroutineScope {
			val unsubscribeDeferred = async {
				messagingRepository.unsubscribe()
			}
			val revokeDeferred = async {
				authRepository.revokeTokens(accessToken = accessToken)
			}

			revokeDeferred.await()
			unsubscribeDeferred.await()
		}

		sessionRepository.clear()
		syncStatusRepository.setSyncStatus(SyncStatus.Healthy)
		applicationRepository.clearData()

		return flowOf(Unit)
	}
}
