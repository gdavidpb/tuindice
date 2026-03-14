package com.gdavidpb.tuindice.auth.domain.usecase

import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
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
	private val credentialsRepository: CredentialsRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		coroutineScope {
			val unsubscribeDeferred = async {
				messagingRepository.unsubscribe()
			}
			val revokeDeferred = async {
				authRepository.revokeTokens()
			}

			revokeDeferred.await()
			unsubscribeDeferred.await()
		}

		sessionRepository.clear()
		credentialsRepository.clearPassword()
		applicationRepository.clearData()

		return flowOf(Unit)
	}
}
