package com.gdavidpb.tuindice.about.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.presentation.machine.SettingsExternalUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsInternalEvent
import com.gdavidpb.tuindice.about.presentation.machine.SettingsMachine
import com.gdavidpb.tuindice.about.testing.ANA_PROFILE_IDENTITY
import com.gdavidpb.tuindice.about.testing.CURRENT_PRODUCTION_VERSION_TEXT
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeProfileIdentityRepository
import com.gdavidpb.tuindice.about.testing.FakeSettingsTextProvider
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.about.testing.fakeObserveProfileIdentityUseCase
import com.gdavidpb.tuindice.about.testing.fakeSettingsAssistantUseCases
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SettingsViewModelContractTest {
	@Test
	fun initialAction_loadsVersionIntoContentState() = runTest {
		val viewModel = createViewModel(testScheduler = testScheduler)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				val content = awaitUntilState<Settings.State.Content>()
				assertEquals(CURRENT_PRODUCTION_VERSION_TEXT, content.versionText)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun content_followsTheIdentityTheDeviceHolds() = runTest {
		val identities = FakeProfileIdentityRepository()
		val viewModel = createViewModel(testScheduler = testScheduler, profileIdentityRepository = identities)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				val named = awaitUntilState<Settings.State.Content> { content -> content.identity != null }
				assertEquals(ANA_PROFILE_IDENTITY, named.identity)

				// A re-upload rolls the picture version: the content follows without asking for anything.
				identities.identity.value = ANA_PROFILE_IDENTITY.copy(pictureVersion = 3)
				val reuploaded = awaitUntilState<Settings.State.Content> { content ->
					content.identity?.pictureVersion == 3
				}
				assertEquals(3, reuploaded.identity?.pictureVersion)

				// The session ended: no user is stored, and the content keeps nothing of the account that left.
				identities.identity.value = null
				val cleared = awaitUntilState<Settings.State.Content> { content -> content.identity == null }
				assertEquals(null, cleared.identity)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun requestSignOut_asksTheHostForTheFlowWithoutChangingTheState() = runTest {
		val viewModel = createViewModel(testScheduler = testScheduler)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.effect.test {
				viewModel.dispatchAction(Settings.Action.RequestSignOut)

				assertEquals(Settings.Effect.RequestSignOut, awaitItem())

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun openAbout_asksTheHostForTheAboutScreen() = runTest {
		val viewModel = createViewModel(testScheduler = testScheduler)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.effect.test {
				viewModel.dispatchAction(Settings.Action.OpenAbout)

				assertEquals(Settings.Effect.NavigateToAbout, awaitItem())

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		// The machine is rebuilt with the same stubs the fixture wires into the
		// view model, because the walk drives the table directly.
		val screenMachine = SettingsMachine(
			loadVersionUseCase = LoadVersionUseCase(
				aboutRepository = FakeAboutRepository(),
				reportingRepository = RecordingReportingRepository()
			),
			externalUseCases = SettingsExternalUseCases(
				sendSupportEmail = SendSupportEmailUseCase(
					configRepository = FakeConfigRepository(),
					deviceInfoRepository = FakeDeviceInfoRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				openStore = OpenStoreUseCase(
					storeUrlRepository = FakeStoreUrlDataSource(),
					reportingRepository = RecordingReportingRepository()
				),
				openExternalUrl = OpenExternalUrlUseCase(
					browserRepository = RecordingBrowserRepository(),
					reportingRepository = RecordingReportingRepository()
				)
			),
			observeProfileIdentityUseCase = fakeObserveProfileIdentityUseCase(),
			appEnvironmentRepository = FakeAppEnvironmentRepository(),
			usageDataConsentRepository = InMemoryUsageDataConsentRepository(),
			assistantUseCases = fakeSettingsAssistantUseCases(),
			// Fixed texts: the walk runs on the host, where the platform resources of the default ones cannot be read.
			textProvider = FakeSettingsTextProvider()
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				Settings.Action.LoadSettings,
				Settings.Action.OpenTermsAndConditions,
				Settings.Action.OpenPrivacyPolicy,
				Settings.Action.OpenSupport,
				Settings.Action.OpenUrl(url = "https://tuindice.app"),
				Settings.Action.RateOnStore,
				Settings.Action.ReportBug,
				Settings.Action.OpenAbout,
				Settings.Action.RequestSignOut,
				Settings.Action.OpenAssistantMemory,
				Settings.Action.ShareApp,
				Settings.Action.SetUsageDataCollectionEnabled(enabled = true),
				Settings.Action.RequestDeleteAssistantConversations,
				Settings.Action.ConfirmDeleteAssistantConversations,
				SettingsInternalEvent.SettingsVersionLoaded(
					versionText = CURRENT_PRODUCTION_VERSION_TEXT,
					usageDataCollectionEnabled = false
				),
				SettingsInternalEvent.SettingsVersionLoadFailed(
					versionFallbackText = "—",
					usageDataCollectionEnabled = false
				),
				SettingsInternalEvent.ProfileIdentityObserved(identity = ANA_PROFILE_IDENTITY),
				SettingsInternalEvent.ProfileIdentityObserved(identity = null),
				SettingsInternalEvent.SupportEmailUriLoaded(uri = "mailto:support@tuindice.app"),
				SettingsInternalEvent.StoreUriLoaded(uri = "https://example.com/store"),
				SettingsInternalEvent.AssistantConversationsDeleted(message = "deleted"),
				SettingsInternalEvent.AssistantConversationsDeleteFailed(message = "failed")
			),
			coroutineScope = backgroundScope,
			// Conservative floor: every internal event is sampled by hand; raise to the
			// observed coverage once the walk has run on CI.
			minRowCoverage = 0.4
		)
	}

	private fun createViewModel(
		testScheduler: TestCoroutineScheduler,
		browserRepository: RecordingBrowserRepository = RecordingBrowserRepository(),
		profileIdentityRepository: FakeProfileIdentityRepository = FakeProfileIdentityRepository()
	): SettingsViewModel {
		return SettingsViewModel(
			screenMachine = SettingsMachine(
				loadVersionUseCase = LoadVersionUseCase(
					aboutRepository = FakeAboutRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				externalUseCases = SettingsExternalUseCases(
					sendSupportEmail = SendSupportEmailUseCase(
						configRepository = FakeConfigRepository(),
						deviceInfoRepository = FakeDeviceInfoRepository(),
						reportingRepository = RecordingReportingRepository()
					),
					openStore = OpenStoreUseCase(
						storeUrlRepository = FakeStoreUrlDataSource(),
						reportingRepository = RecordingReportingRepository()
					),
					openExternalUrl = OpenExternalUrlUseCase(
						browserRepository = browserRepository,
						reportingRepository = RecordingReportingRepository()
					)
				),
				observeProfileIdentityUseCase = fakeObserveProfileIdentityUseCase(profileIdentityRepository),
				appEnvironmentRepository = FakeAppEnvironmentRepository(),
				usageDataConsentRepository = InMemoryUsageDataConsentRepository(),
				assistantUseCases = fakeSettingsAssistantUseCases()
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = TestTuIndiceDispatchers(UnconfinedTestDispatcher(testScheduler))
		)
	}
}
