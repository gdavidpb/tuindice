package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.model.StartUpTarget
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.domain.usecase.error.StartUpUseCaseError
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.domain.usecase.result.StartUpResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class StartUpUseCase(
	private val sessionRepository: SessionRepository,
	private val settingsRepository: SettingsRepository,
	private val configRepository: ConfigRepository,
	private val deviceInfoRepository: DeviceInfoRepository,
	private val applicationRepository: ApplicationRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: StartUpExceptionHandler
) : FlowUseCase<Unit, StartUpResult, StartUpUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<StartUpResult> {
		val outdatedAppState = settingsRepository.getOutdatedAppState()
		if (outdatedAppState != null) {
			if (deviceInfoRepository.appVersionCode() < outdatedAppState.minimumVersionCode) {
				return flowOf(StartUpResult.OutdatedApp(outdatedAppState))
			}

			settingsRepository.clearOutdatedAppState()
		}

		configRepository.tryFetch()

		val appAvailabilityNotice = configRepository.getAppAvailabilityNotice()
		if (appAvailabilityNotice.enabled) {
			return flowOf(
				StartUpResult.AppUnavailable(
					notice = appAvailabilityNotice
				)
			)
		}

		val startUpResult = runCatching {
			val hasActiveTokens = sessionRepository.hasActiveSession()

			val startTarget = if (hasActiveTokens)
				StartUpTarget.Main(section = settingsRepository.getLastMainSection())
			else
				StartUpTarget.Auth

			StartUpResult.Available(
				startTarget = startTarget
			)
		}.onFailure {
			applicationRepository.clearData()
		}.getOrThrow()

		return flowOf(startUpResult)
	}
}
