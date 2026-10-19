package com.gdavidpb.tuindice.about.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.about.domain.usecase.DeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantAvailabilityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.presentation.machine.SettingsAssistantUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsContentUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsExternalUseCases
import com.gdavidpb.tuindice.about.presentation.machine.SettingsMachine
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantAvailabilityRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantConsentRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantConversationsRepository
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.about.testing.fakeObserveProfileIdentityUseCase
import com.gdavidpb.tuindice.about.testing.fakeRefreshAssistantConsentUseCase
import com.gdavidpb.tuindice.about.testing.fakeSetAssistantConsentUseCase
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsAssistantViewModelContractTest {
	@Test
	fun initialState_isIdleUntilTheVersionLoads() {
		val machine = createMachine(
			assistantAvailability = FakeAssistantAvailabilityRepository(),
			assistantConsent = FakeAssistantConsentRepository()
		)

		assertEquals(Settings.State.Idle, machine.initialState())
	}

	@Test
	fun assistantRows_areHiddenWhenTheAssistantIsUnavailable() = runTest {
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			assistantAvailability = FakeAssistantAvailabilityRepository(visible = false)
		)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		val content = assertIs<Settings.State.Content>(viewModel.state.value)
		assertFalse(content.assistantAvailable)
	}

	@Test
	fun assistantRows_reflectTheObservedAvailabilityAndConsent() = runTest {
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			assistantAvailability = FakeAssistantAvailabilityRepository(visible = true),
			assistantConsent = FakeAssistantConsentRepository(granted = true)
		)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		val content = assertIs<Settings.State.Content>(viewModel.state.value)
		assertTrue(content.assistantAvailable)
		assertTrue(content.assistantConsentGranted)
	}

	@Test
	fun assistantConsent_isReadFromTheServerWhenTheDeviceHoldsNone() = runTest {
		// A reinstall, a new device or a sign-out leave the device without the consent the server still holds.
		val assistantConsent = FakeAssistantConsentRepository(granted = false, serverGranted = true)
		val viewModel = createViewModel(testScheduler = testScheduler, assistantConsent = assistantConsent)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		assertEquals(1, assistantConsent.refreshes)
		assertTrue(assertIs<Settings.State.Content>(viewModel.state.value).assistantConsentGranted)
	}

	@Test
	fun assistantConsent_staysAsTheDeviceHoldsItWhenTheServerCannotBeRead() = runTest {
		val assistantConsent = FakeAssistantConsentRepository(granted = true, refreshFails = true)
		val viewModel = createViewModel(testScheduler = testScheduler, assistantConsent = assistantConsent)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		assertEquals(1, assistantConsent.refreshes)
		assertTrue(assertIs<Settings.State.Content>(viewModel.state.value).assistantConsentGranted)
	}

	@Test
	fun assistantConsent_isNotAskedOfTheServerWhenTheAssistantIsUnavailable() = runTest {
		val assistantConsent = FakeAssistantConsentRepository(granted = false, serverGranted = true)
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			assistantAvailability = FakeAssistantAvailabilityRepository(visible = false),
			assistantConsent = assistantConsent
		)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		assertEquals(0, assistantConsent.refreshes)
		assertFalse(assertIs<Settings.State.Content>(viewModel.state.value).assistantConsentGranted)
	}

	@Test
	fun setAssistantConsent_movesTheSwitchAndStoresTheChoice() = runTest {
		val assistantConsent = FakeAssistantConsentRepository(granted = false)
		val viewModel = createViewModel(testScheduler = testScheduler, assistantConsent = assistantConsent)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		viewModel.dispatchAction(Settings.Action.SetAssistantConsent(granted = true))
		testScheduler.advanceUntilIdle()

		assertTrue(assistantConsent.isAssistantConsentGranted())
		assertTrue(assertIs<Settings.State.Content>(viewModel.state.value).assistantConsentGranted)
	}

	@Test
	fun openAssistantMemory_emitsTheNavigateEffect() = runTest {
		val viewModel = createViewModel(testScheduler = testScheduler)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		viewModel.dispatchAction(Settings.Action.OpenAssistantMemory)

		viewModel.effect.test {
			assertEquals(Settings.Effect.NavigateToAssistantMemory, awaitItem())

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun requestDeleteAssistantConversations_asksForTheConfirmationWithoutDeleting() = runTest {
		val assistantConversations = FakeAssistantConversationsRepository()
		val viewModel = createViewModel(testScheduler = testScheduler, assistantConversations = assistantConversations)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		viewModel.dispatchAction(Settings.Action.RequestDeleteAssistantConversations)
		testScheduler.advanceUntilIdle()

		assertEquals(0, assistantConversations.deletions)
		viewModel.effect.test {
			assertEquals(Settings.Effect.NavigateToDeleteAssistantConversationsConfirmation, awaitItem())

			cancelAndIgnoreRemainingEvents()
		}
	}

	private fun createViewModel(
		testScheduler: TestCoroutineScheduler,
		assistantAvailability: FakeAssistantAvailabilityRepository = FakeAssistantAvailabilityRepository(),
		assistantConsent: FakeAssistantConsentRepository = FakeAssistantConsentRepository(),
		assistantConversations: FakeAssistantConversationsRepository = FakeAssistantConversationsRepository()
	): SettingsViewModel {
		return SettingsViewModel(
			screenMachine = createMachine(
				assistantAvailability = assistantAvailability,
				assistantConsent = assistantConsent,
				assistantConversations = assistantConversations
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = TestTuIndiceDispatchers(UnconfinedTestDispatcher(testScheduler))
		)
	}

	private fun createMachine(
		assistantAvailability: FakeAssistantAvailabilityRepository,
		assistantConsent: FakeAssistantConsentRepository,
		assistantConversations: FakeAssistantConversationsRepository = FakeAssistantConversationsRepository()
	): SettingsMachine {
		return SettingsMachine(
			contentUseCases = SettingsContentUseCases(
				loadVersion = LoadVersionUseCase(
					aboutRepository = FakeAboutRepository(),
					reportingRepository = RecordingReportingRepository()
				),
				observeProfileIdentity = fakeObserveProfileIdentityUseCase()
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
			appEnvironmentRepository = FakeAppEnvironmentRepository(),
			usageDataConsentRepository = InMemoryUsageDataConsentRepository(),
			assistantUseCases = SettingsAssistantUseCases(
				observeAvailability = ObserveAssistantAvailabilityUseCase(
					assistantAvailabilityRepository = assistantAvailability,
					reportingRepository = RecordingReportingRepository()
				),
				observeConsent = ObserveAssistantConsentUseCase(
					assistantConsentRepository = assistantConsent,
					reportingRepository = RecordingReportingRepository()
				),
				setConsent = fakeSetAssistantConsentUseCase(assistantConsent),
				refreshConsent = fakeRefreshAssistantConsentUseCase(assistantConsent),
				deleteConversations = DeleteAssistantConversationsUseCase(
					assistantConversationsRepository = assistantConversations,
					reportingRepository = RecordingReportingRepository()
				)
			)
		)
	}
}
