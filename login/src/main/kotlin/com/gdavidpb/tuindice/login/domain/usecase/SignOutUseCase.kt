package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.DependenciesRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignOutUseCase(
	private val authRepository: AuthRepository,
	private val messagingRepository: MessagingRepository,
	private val dependenciesRepository: DependenciesRepository,
	private val applicationRepository: ApplicationRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		messagingRepository.unsubscribeFromAllTopics()
		authRepository.signOut()
		applicationRepository.clearData()
		dependenciesRepository.restart()

		return flowOf(Unit)
	}
}