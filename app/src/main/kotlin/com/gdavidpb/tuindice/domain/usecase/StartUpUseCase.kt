package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.exception.ServicesUnavailableException
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.domain.repository.MobileServicesRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.domain.usecase.result.StartUpResult
import com.gdavidpb.tuindice.domain.usecase.error.StartUpUseCaseError
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.login.presentation.navigation.LoginDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class StartUpUseCase(
	private val authRepository: AuthRepository,
	private val settingsRepository: SettingsRepository,
	private val messagingRepository: MessagingRepository,
	private val mobileServicesRepository: MobileServicesRepository,
	private val configRepository: ConfigRepository,
	override val exceptionHandler: StartUpExceptionHandler
) : FlowUseCase<Unit, StartUpResult, StartUpUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<StartUpResult> {
		val servicesStatus = mobileServicesRepository.getServicesStatus()

		check(servicesStatus.isAvailable) {
			throw ServicesUnavailableException(servicesStatus)
		}

		noAwait { configRepository.tryFetch() }
		noAwait { messagingRepository.enroll() }

		val isActiveAuth = authRepository.isActiveAuth()

		val startDestination = if (isActiveAuth)
			settingsRepository.getLastDestination()
		else
			LoginDestination.NavGraph

		val startUpResult = StartUpResult(
			startDestination = startDestination
		)

		return flowOf(startUpResult)
	}
}