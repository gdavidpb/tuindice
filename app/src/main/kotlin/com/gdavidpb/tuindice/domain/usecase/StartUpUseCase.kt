package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.exception.ServicesUnavailableException
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.MobileServicesRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.suspendNoAwait
import com.gdavidpb.tuindice.domain.model.StartUpData
import com.gdavidpb.tuindice.domain.usecase.error.StartUpError
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class StartUpUseCase(
	private val authRepository: AuthRepository,
	private val settingsRepository: SettingsRepository,
	private val mobileServicesRepository: MobileServicesRepository,
	private val configRepository: ConfigRepository,
	override val exceptionHandler: StartUpExceptionHandler
) : FlowUseCase<String?, StartUpData, StartUpError>() {
	override suspend fun executeOnBackground(params: String?): Flow<StartUpData> {
		val servicesStatus = mobileServicesRepository.getServicesStatus()

		check(servicesStatus.isAvailable) {
			throw ServicesUnavailableException(servicesStatus)
		}

		suspendNoAwait {
			runCatching { configRepository.tryFetch() }
		}

		val isActiveAuth = authRepository.isActiveAuth()

		// TODO get from repository
		val destinations = listOf(
			Destination.Summary,
			Destination.Record,
			Destination.About,
			Destination.Browser
		)

		val startDestination = if (isActiveAuth) {
			val lastScreen = settingsRepository.getLastScreen()
			val activeToken = authRepository.getActiveToken()

			settingsRepository.setActiveToken(token = activeToken)

			destinations.first { destination ->
				destination.route == lastScreen
			}
		} else {
			Destination.SignIn
		}

		val startUpData = StartUpData(
			startDestination = startDestination,
			currentDestination = startDestination,
			destinations = destinations
		)

		return flowOf(startUpData)
	}
}