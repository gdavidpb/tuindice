package com.gdavidpb.tuindice.presentation.machine

import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.model.UpdateLaunchResult
import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.ScheduleSyncUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastMainSectionUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testing.FakeCoreCacheStateRepository
import com.gdavidpb.tuindice.testing.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testing.createBrowserViewModel
import com.gdavidpb.tuindice.testing.createMainViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeUpdateRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

// Model-based walks over the host machines: the wiring mirrors the fixtures in
// com.gdavidpb.tuindice.testing.MaincoreUiTestFixtures, rebuilt inline because the
// walk needs the ScreenMachine itself rather than the view model.
class MainStateMachineContractTest {
	@Test
	fun mainMachine_survivesSeededRandomWalk() = runTest {
		val sessionRepository = FakeSessionRepository()
		val settingsRepository = FakeSettingsRepository(
			reviewSuggested = true,
			lastMainSection = MainSection.SUMMARY
		)
		val configRepository = FakeConfigRepository()
		val applicationRepository = RecordingApplicationRepository()
		val reportingRepository = RecordingReportingRepository()

		val screenMachine = MainMachine(
			startUpUseCase = StartUpUseCase(
				sessionRepository = sessionRepository,
				settingsRepository = settingsRepository,
				configRepository = configRepository,
				deviceInfoRepository = FakeDeviceInfoRepository(),
				applicationRepository = applicationRepository,
				reportingRepository = reportingRepository,
				exceptionHandler = StartUpExceptionHandler()
			),
			requestReviewUseCase = RequestReviewUseCase(
				settingsRepository = settingsRepository,
				configRepository = configRepository,
				reportingRepository = reportingRepository
			),
			getUpdateInfoUseCase = GetUpdateInfoUseCase(
				configRepository = configRepository,
				updateGateway = FakeUpdateRepository(),
				reportingRepository = reportingRepository
			),
			scheduleSyncUseCase = ScheduleSyncUseCase(
				sessionRepository = sessionRepository,
				credentialsRepository = FakeCredentialsRepository(),
				syncRepository = FakeSyncRepository(),
				coreCacheStateRepository = FakeCoreCacheStateRepository(),
				reportingRepository = reportingRepository
			),
			setLastMainSectionUseCase = SetLastMainSectionUseCase(
				settingsRepository = settingsRepository,
				reportingRepository = reportingRepository
			)
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				Main.Action.StartUp,
				Main.Action.ShowOutdatedApp(
					outdatedAppState = OutdatedAppState(minimumVersionCode = 52)
				),
				Main.Action.RequestReview,
				Main.Action.RequestUpdateCheck,
				Main.Action.UpdateFlowCompleted(
					result = UpdateLaunchResult.OpenStoreFallback(
						primaryUrl = "market://details?id=com.gdavidpb.tuindice",
						fallbackUrl = "https://play.google.com/store/apps/details?id=com.gdavidpb.tuindice"
					)
				),
				Main.Action.RequestSync,
				Main.Action.SetLastMainSection(section = MainSection.SUMMARY),
				MainInternalEvent.StartUpStarting,
				MainInternalEvent.StartUpCompleted(
					startDestination = SummaryDestination.NavGraph
				),
				MainInternalEvent.AppUnavailableResolved(
					notice = AppAvailabilityNotice(
						enabled = true,
						title = "Mantenimiento",
						message = "Volvemos pronto."
					)
				),
				MainInternalEvent.OutdatedAppResolved(
					outdatedAppState = OutdatedAppState(minimumVersionCode = 52)
				),
				MainInternalEvent.StartUpFailed(noServices = false),
				MainInternalEvent.ReviewRequested,
				MainInternalEvent.UpdateInfoLoaded(action = UpdateAction.Immediate)
			),
			scope = backgroundScope,
			// Conservative floor: every internal event is sampled by hand; raise to the
			// observed coverage once the walk has run on CI.
			minRowCoverage = 0.4
		)
	}

	@Test
	fun browserMachine_survivesSeededRandomWalk() = runTest {
		assertMachineRandomWalk(
			screenMachine = BrowserMachine(),
			sampleEvents = listOf(
				Browser.Action.NavigateTo(
					title = "Términos y condiciones",
					url = "https://tuindice.app/terms"
				),
				Browser.Action.SetLoading(isLoading = false),
				Browser.Action.OpenExternalResource(url = "https://example.com/resource")
			),
			scope = backgroundScope,
			// Conservative floor: all three rows resolve once NavigateTo lands on Content;
			// raise to the observed coverage once the walk has run on CI.
			minRowCoverage = 0.5
		)
	}

	@Test
	fun machines_exportDeclaredTransitionsToMermaid() {
		val mainDiagram = createMainViewModel().machine.exportToMermaid(
			machineName = "main",
			initialState = Main.State.Starting::class
		)
		val browserDiagram = createBrowserViewModel().machine.exportToMermaid(
			machineName = "browser",
			initialState = Browser.State.Idle::class
		)

		// Captured from test output to publish the generated diagrams as docs artifacts.
		println(mainDiagram)
		println(browserDiagram)

		val expectedMainFragments = listOf(
			"starting",
			"content",
			"failed",
				"app_unavailable",
				"StartUp",
				"ShowOutdatedApp",
				"StartUpCompleted",
			"AppUnavailableResolved",
			"OutdatedAppResolved",
			"ReviewRequested / TriggerReviewFlow",
			"UpdateFlowCompleted / OpenUpdateStoreFallback"
		)

		for (fragment in expectedMainFragments) {
			assertTrue(
				mainDiagram.contains(fragment),
				"Expected main Mermaid export to mention '$fragment':\n$mainDiagram"
			)
		}

		val expectedBrowserFragments = listOf(
			"idle",
			"content",
			"NavigateTo",
			"SetLoading",
			"OpenExternalResource / NavigateToExternalResourceDialog"
		)

		for (fragment in expectedBrowserFragments) {
			assertTrue(
				browserDiagram.contains(fragment),
				"Expected browser Mermaid export to mention '$fragment':\n$browserDiagram"
			)
		}
	}
}
