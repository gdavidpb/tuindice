package com.gdavidpb.tuindice.about.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.machine.AboutInternalEvent
import com.gdavidpb.tuindice.about.presentation.machine.AboutMachine
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.about.testing.CURRENT_PRODUCTION_VERSION_TEXT
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AboutViewModelContractTest {
	@Test
	fun initialAction_loadsVersionIntoContentState() = runTest {
		createViewModel().state.test {
			val initial = awaitItem()
			assertEquals(About.State.Idle, initial)

			val content = assertIs<About.State.Content>(awaitItem())
			assertEquals(CURRENT_PRODUCTION_VERSION_TEXT, content.versionText)

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		// The machine is rebuilt with the same stubs the fixture wires into the
		// view model, because the walk drives the table directly.
		val screenMachine = AboutMachine(
			loadVersionUseCase = LoadVersionUseCase(
				aboutRepository = FakeAboutRepository(),
				reportingRepository = RecordingReportingRepository()
			),
			sendSupportEmailUseCase = SendSupportEmailUseCase(
				configRepository = FakeConfigRepository(),
				reportingRepository = RecordingReportingRepository()
			),
			openStoreUseCase = OpenStoreUseCase(
				storeUrlRepository = FakeStoreUrlDataSource(),
				reportingRepository = RecordingReportingRepository()
			),
			openExternalUrlUseCase = OpenExternalUrlUseCase(
				browserRepository = RecordingBrowserRepository(),
				reportingRepository = RecordingReportingRepository()
			),
			appEnvironmentRepository = FakeAppEnvironmentRepository(),
			usageDataConsentRepository = InMemoryUsageDataConsentRepository()
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				About.Action.LoadVersion,
				About.Action.OpenTermsAndConditions,
				About.Action.OpenPrivacyPolicy,
				About.Action.OpenSupport,
				About.Action.OpenUrl(url = "https://tuindice.app"),
				About.Action.RateOnStore,
				About.Action.ReportBug,
				About.Action.ContactDeveloper,
				About.Action.ShareApp,
				About.Action.SetUsageDataCollectionEnabled(enabled = true),
				AboutInternalEvent.AboutVersionLoaded(
					versionText = CURRENT_PRODUCTION_VERSION_TEXT,
					usageDataCollectionEnabled = false
				),
				AboutInternalEvent.AboutVersionLoadFailed,
				AboutInternalEvent.SupportEmailUriLoaded(uri = "mailto:support@tuindice.app"),
				AboutInternalEvent.StoreUriLoaded(uri = "https://example.com/store")
			),
			scope = backgroundScope,
			// Conservative floor: every internal event is sampled by hand; raise to the
			// observed coverage once the walk has run on CI.
			minRowCoverage = 0.4
		)
	}

	private fun createViewModel(
		browserRepository: RecordingBrowserRepository = RecordingBrowserRepository()
	): AboutViewModel {
		return AboutViewModel(
			screenMachine = AboutMachine(
				loadVersionUseCase = LoadVersionUseCase(
					aboutRepository = FakeAboutRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				sendSupportEmailUseCase = SendSupportEmailUseCase(
					configRepository = FakeConfigRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				openStoreUseCase = OpenStoreUseCase(
					storeUrlRepository = FakeStoreUrlDataSource(),
					reportingRepository = RecordingReportingRepository()
				),
				openExternalUrlUseCase = OpenExternalUrlUseCase(
					browserRepository = browserRepository,
					reportingRepository = RecordingReportingRepository()
				),
				appEnvironmentRepository = FakeAppEnvironmentRepository(),
				usageDataConsentRepository = InMemoryUsageDataConsentRepository()
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
