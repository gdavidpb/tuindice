package com.gdavidpb.tuindice.base.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.domain.usecase.base.CompletableUseCase
import com.gdavidpb.tuindice.base.utils.ComputationManager
import com.gdavidpb.tuindice.base.utils.Topics

class SignOutUseCase(
	private val authRepository: AuthRepository,
	private val messagingRepository: MessagingRepository,
	private val databaseRepository: DatabaseRepository,
	private val settingsRepository: SettingsRepository,
	private val storageRepository: StorageRepository,
	private val dependenciesRepository: DependenciesRepository
) : CompletableUseCase<Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit) {
		messagingRepository.unsubscribeFromTopic(Topics.TOPIC_GENERAL)
		authRepository.signOut()
		settingsRepository.clear()
		databaseRepository.close()
		databaseRepository.clearCache()
		storageRepository.clear()
		dependenciesRepository.restart()
		ComputationManager.clearCache()
	}
}