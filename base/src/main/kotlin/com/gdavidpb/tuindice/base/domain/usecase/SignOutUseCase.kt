package com.gdavidpb.tuindice.base.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.domain.usecase.base.CompletableUseCase

class SignOutUseCase(
	private val authRepository: AuthRepository,
	private val messagingRepository: MessagingRepository,
	private val dependenciesRepository: DependenciesRepository,
	private val applicationRepository: ApplicationRepository,
	override val configRepository: ConfigRepository,
	override val reportingRepository: ReportingRepository
) : CompletableUseCase<Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit) {
		messagingRepository.unsubscribeFromAllTopics()
		authRepository.signOut()
		applicationRepository.clearData()
		dependenciesRepository.restart()
	}
}