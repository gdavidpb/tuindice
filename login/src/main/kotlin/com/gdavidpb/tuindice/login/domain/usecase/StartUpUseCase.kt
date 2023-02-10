package com.gdavidpb.tuindice.login.domain.usecase

import com.gdavidpb.tuindice.base.domain.exception.ServicesUnavailableException
import com.gdavidpb.tuindice.base.domain.model.StartUpAction
import com.gdavidpb.tuindice.base.domain.repository.*
import com.gdavidpb.tuindice.base.domain.usecase.baseV2.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.login.domain.error.StartUpError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class StartUpUseCase(
	private val authRepository: AuthRepository,
	private val settingsRepository: SettingsRepository,
	private val networkRepository: NetworkRepository,
	private val applicationRepository: ApplicationRepository,
	private val mobileServicesRepository: MobileServicesRepository,
	private val configRepository: ConfigRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, StartUpAction, StartUpError>() {
	override suspend fun executeOnBackground(params: String): Flow<StartUpAction> {
		val servicesStatus = mobileServicesRepository.getServicesStatus()

		check(servicesStatus.isAvailable) {
			throw ServicesUnavailableException(servicesStatus)
		}

		noAwait { configRepository.tryFetch() }

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