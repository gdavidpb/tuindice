package com.gdavidpb.tuindice.base.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.domain.usecase.base.CompletableUseCase
import com.gdavidpb.tuindice.base.utils.Topics

class SignOutUseCase(
	private val authRepository: AuthRepository,
	private val messagingRepository: MessagingRepository,
	private val settingsRepository: SettingsRepository,
	private val dependenciesRepository: DependenciesRepository,
	private val applicationRepository: ApplicationRepository
) : CompletableUseCase<Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit) {
		messagingRepository.unsubscribeFromTopic(Topics.TOPIC_GENERAL)
		authRepository.signOut()
		settingsRepository.clear()
		// TODO persistenceRepository.close()
		// TODO storageRepository.clear()
		dependenciesRepository.restart()
		// TODO ComputationManager.clearCache()
	}

	override suspend fun executeOnException(throwable: Throwable): Nothing? {
		applicationRepository.clearData()

		return null
	}
}