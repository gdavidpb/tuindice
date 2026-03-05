package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastDestinationUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.presentation.action.browser.NavigateToActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.OpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.SetLoadingActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestReviewActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.RequestUpdateActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.SetLastDestinationActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.StartUpActionProcessor
import com.gdavidpb.tuindice.presentation.action.main.UpdateStateActionProcessor
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeUpdateRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository

fun createBrowserViewModel(): BrowserViewModel = BrowserViewModel(
	navigateToActionProcessor = NavigateToActionProcessor(),
	setLoadingActionProcessor = SetLoadingActionProcessor(),
	openExternalResourceActionProcessor = OpenExternalResourceActionProcessor()
)

fun createMainViewModel(
	sessionRepository: SessionRepository = FakeSessionRepository(),
	settingsRepository: FakeSettingsRepository = FakeSettingsRepository(
		reviewSuggested = true,
		lastDestination = MainDestination.GooglePlayServicesUnavailableDialog
	),
	configRepository: FakeConfigRepository = FakeConfigRepository(),
	updateRepository: FakeUpdateRepository = FakeUpdateRepository(),
	applicationRepository: RecordingApplicationRepository = RecordingApplicationRepository(),
	reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
): MainViewModel {
	return MainViewModel(
		updateStateActionProcessor = UpdateStateActionProcessor(),
		startUpActionProcessor = StartUpActionProcessor(
			startUpUseCase = StartUpUseCase(
				sessionRepository = sessionRepository,
				settingsRepository = settingsRepository,
				configRepository = configRepository,
				exceptionHandler = StartUpExceptionHandler(
					applicationRepository = applicationRepository,
					reportingRepository = reportingRepository
				)
			)
		),
		requestReviewActionProcessor = RequestReviewActionProcessor(
			requestReviewUseCase = RequestReviewUseCase(
				settingsRepository = settingsRepository,
				configRepository = configRepository
			)
		),
		requestUpdateActionProcessor = RequestUpdateActionProcessor(
			getUpdateInfoUseCase = GetUpdateInfoUseCase(
				configRepository = configRepository,
				updateGateway = updateRepository
			)
		),
		setLastDestinationActionProcessor = SetLastDestinationActionProcessor(
			setLastDestinationUseCase = SetLastDestinationUseCase(
				settingsRepository = settingsRepository
			)
		)
	)
}

class FakeDeviceInfoRepository(
	private val hasCamera: Boolean = false,
	private val versionName: String = "1.0.0",
	private val versionCode: Long = 1L
) : DeviceInfoRepository {
	override fun appVersionName(): String = versionName

	override fun appVersionCode(): Long = versionCode

	override fun hasCamera(): Boolean = hasCamera
}
