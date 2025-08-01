package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.domain.usecase.error.StartUpUseCaseError
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.domain.usecase.result.StartUpResult
import com.gdavidpb.tuindice.login.presentation.navigation.LoginDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class StartUpUseCase(
	private val sessionRepository: SessionRepository,
	private val settingsRepository: SettingsRepository,
	private val configRepository: ConfigRepository,
	override val exceptionHandler: StartUpExceptionHandler
) : FlowUseCase<Unit, StartUpResult, StartUpUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<StartUpResult> {
		noAwait { configRepository.tryFetch() }

		val hasActiveTokens = sessionRepository.hasActiveSession()

		val startDestination = if (hasActiveTokens)
			settingsRepository.getLastDestination()
		else
			LoginDestination.NavGraph

		val startUpResult = StartUpResult(
			startDestination = startDestination
		)

		return flowOf(startUpResult)
	}
}