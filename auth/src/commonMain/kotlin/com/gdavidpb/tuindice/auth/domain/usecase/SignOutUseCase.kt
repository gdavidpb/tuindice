package com.gdavidpb.tuindice.auth.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignOutUseCase(
	private val sessionRepository: SessionRepository,
	private val messagingRepository: MessagingRepository,
	private val applicationRepository: ApplicationRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		messagingRepository.unsubscribe()
		sessionRepository.clear()
		applicationRepository.clearData()

		return flowOf(Unit)
	}
}
