package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignOutUseCase(
	private val sessionRepository: SessionRepository,
	private val messagingRepository: MessagingRepository,
	private val applicationRepository: ApplicationRepository,
	private val dependenciesRepository: DependenciesRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		messagingRepository.unsubscribe()
		sessionRepository.clear()
		applicationRepository.clearData()
		dependenciesRepository.restart()

		return flowOf(Unit)
	}
}
