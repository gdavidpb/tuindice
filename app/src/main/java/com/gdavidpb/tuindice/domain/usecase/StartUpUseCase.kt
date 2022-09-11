package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.model.exception.ServicesUnavailableException
import com.gdavidpb.tuindice.domain.repository.*
import com.gdavidpb.tuindice.domain.usecase.coroutines.ResultUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.StartUpError
import com.gdavidpb.tuindice.utils.extensions.isConnection

class StartUpUseCase(
	private val servicesRepository: MobileServicesRepository,
	private val settingsRepository: SettingsRepository,
	private val authRepository: AuthRepository,
	private val databaseRepository: DatabaseRepository,
	private val networkRepository: NetworkRepository
) : ResultUseCase<String, StartUpAction, StartUpError>() {
	override suspend fun executeOnBackground(params: String): StartUpAction {
		val servicesStatus = servicesRepository.getServicesStatus()

		check(servicesStatus.isAvailable) {
			throw ServicesUnavailableException(servicesStatus)
		}

		val isActiveAuth = authRepository.isActiveAuth()

		return if (isActiveAuth) {
			val lastScreen = settingsRepository.getLastScreen()
			val activeAuth = authRepository.getActiveAuth()
			val hasCache = databaseRepository.hasCache(uid = activeAuth.uid)
			val activeAccount = databaseRepository.getAccount(uid = activeAuth.uid)

			check(hasCache) { "handleSignedIn no cache" }

			StartUpAction.Main(screen = lastScreen, account = activeAccount)
		} else
			StartUpAction.SignIn
	}

	override suspend fun executeOnException(throwable: Throwable): StartUpError? {
		return when {
			throwable is ServicesUnavailableException -> StartUpError.NoServices(throwable.servicesStatus)
			throwable.isConnection() -> StartUpError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}