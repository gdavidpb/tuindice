package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.StartUpAction
import com.gdavidpb.tuindice.base.domain.exception.ServicesUnavailableException
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.login.domain.error.StartUpError

class StartUpUseCase(
	private val authRepository: AuthRepository,
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
			val activeToken = authRepository.getActiveToken()

			settingsRepository.setActiveToken(token = activeToken)

			StartUpAction.Main(screen = lastScreen)
		} else
			StartUpAction.SignIn
	}

	override suspend fun executeOnException(throwable: Throwable): StartUpError? {
		return when {
			throwable is ServicesUnavailableException -> StartUpError.NoServices(throwable.servicesStatus)
			throwable.isConnection() -> StartUpError.NoConnection(networkRepository.isAvailable())
			else -> {
				applicationRepository.clearData()

				super.executeOnException(throwable)
			}
		}
	}
}