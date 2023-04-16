package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.exception.ServicesUnavailableException
import com.gdavidpb.tuindice.base.domain.model.StartUpAction
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.MobileServicesRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.suspendNoAwait
import com.gdavidpb.tuindice.login.domain.usecase.error.StartUpError
import com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler.StartUpExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class StartUpUseCase(
	private val authRepository: AuthRepository,
	private val settingsRepository: SettingsRepository,
	private val mobileServicesRepository: MobileServicesRepository,
	private val configRepository: ConfigRepository,
	override val exceptionHandler: StartUpExceptionHandler
) : FlowUseCase<String, StartUpAction, StartUpError>() {
	override suspend fun executeOnBackground(params: String): Flow<StartUpAction> {
		val servicesStatus = mobileServicesRepository.getServicesStatus()

		check(servicesStatus.isAvailable) {
			throw ServicesUnavailableException(servicesStatus)
		}

		suspendNoAwait {
			runCatching { configRepository.tryFetch() }
		}

		val isActiveAuth = authRepository.isActiveAuth()

		val startUpAction = if (isActiveAuth) {
			val lastScreen = settingsRepository.getLastScreen()
			val activeToken = authRepository.getActiveToken()

			settingsRepository.setActiveToken(token = activeToken)

			StartUpAction.Main(screen = lastScreen)
		} else
			StartUpAction.SignIn

		return flowOf(startUpAction)
	}
}