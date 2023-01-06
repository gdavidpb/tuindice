package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.StartUpAction
import com.gdavidpb.tuindice.base.domain.model.exception.ServicesUnavailableException
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.base.utils.extensions.isConnection
import com.gdavidpb.tuindice.base.utils.extensions.noAwait
import com.gdavidpb.tuindice.login.domain.repository.LocalRepository
import com.gdavidpb.tuindice.login.domain.usecase.error.StartUpError

class StartUpUseCase(
	private val authRepository: AuthRepository,
	private val localRepository: LocalRepository,
	private val settingsRepository: SettingsRepository,
	private val networkRepository: NetworkRepository,
	private val configRepository: ConfigRepository,
	private val applicationRepository: ApplicationRepository,
	private val mobileServicesRepository: MobileServicesRepository
) : ResultUseCase<String, StartUpAction, StartUpError>() {
	override suspend fun executeOnBackground(params: String): StartUpAction {
		val servicesStatus = mobileServicesRepository.getServicesStatus()

		check(servicesStatus.isAvailable) {
			throw ServicesUnavailableException(servicesStatus)
		}

		noAwait { configRepository.tryFetch() }

		val isActiveAuth = authRepository.isActiveAuth()

		return if (isActiveAuth) {
			val lastScreen = settingsRepository.getLastScreen()
			val activeAuth = authRepository.getActiveAuth()
			val activeToken = authRepository.getActiveToken()
			val activeAccount = localRepository.getAccount(uid = activeAuth.uid)

			settingsRepository.setActiveToken(token = activeToken)

			StartUpAction.Main(screen = lastScreen, account = activeAccount)
		} else
			StartUpAction.SignIn
	}

	override suspend fun executeOnException(throwable: Throwable): StartUpError? {
		return when {
			throwable is ServicesUnavailableException -> StartUpError.NoServices(throwable.servicesStatus)
			throwable.isConnection() -> StartUpError.NoConnection(networkRepository.isAvailable())
			else -> {
				applicationRepository.clearData()

				null
			}
		}
	}
}