package com.gdavidpb.tuindice.about.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.about.domain.usecase.DeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantAvailabilityUseCase
import com.gdavidpb.tuindice.about.domain.usecase.ObserveAssistantConsentUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenExternalUrlUseCase
import com.gdavidpb.tuindice.about.domain.usecase.OpenStoreUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SendSupportEmailUseCase
import com.gdavidpb.tuindice.about.domain.usecase.SetAssistantConsentUseCase
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.machine.AboutMachine
import com.gdavidpb.tuindice.about.testing.FakeAboutRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantAvailabilityRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantConsentRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantConversationsRepository
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
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
class AboutAssistantViewModelContractTest {
	@Test
	fun initialState_isIdleUntilTheVersionLoads() {
		val machine = createMachine(
			assistantAvailability = FakeAssistantAvailabilityRepository(),
			assistantConsent = FakeAssistantConsentRepository()
		)

		assertEquals(About.State.Idle, machine.initialState())
	}

	@Test
	fun assistantRows_areHiddenWhenTheAssistantIsUnavailable() = runTest {
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			assistantAvailability = FakeAssistantAvailabilityRepository(visible = false)
		)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		val content = assertIs<About.State.Content>(viewModel.state.value)
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

		val content = assertIs<About.State.Content>(viewModel.state.value)
		assertTrue(content.assistantAvailable)
		assertTrue(content.assistantConsentGranted)
	}

	@Test
	fun setAssistantConsent_movesTheSwitchAndStoresTheChoice() = runTest {
		val assistantConsent = FakeAssistantConsentRepository(granted = false)
		val viewModel = createViewModel(testScheduler = testScheduler, assistantConsent = assistantConsent)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		viewModel.setAssistantConsentAction(granted = true)
		testScheduler.advanceUntilIdle()

		assertTrue(assistantConsent.isAssistantConsentGranted())
		assertTrue(assertIs<About.State.Content>(viewModel.state.value).assistantConsentGranted)
	}

	@Test
	fun openAssistantMemory_emitsTheNavigateEffect() = runTest {
		val viewModel = createViewModel(testScheduler = testScheduler)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		viewModel.openAssistantMemoryAction()

		viewModel.effect.test {
			assertEquals(About.Effect.NavigateToAssistantMemory, awaitItem())

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun requestDeleteAssistantConversations_asksForTheConfirmationWithoutDeleting() = runTest {
		val assistantConversations = FakeAssistantConversationsRepository()
		val viewModel = createViewModel(testScheduler = testScheduler, assistantConversations = assistantConversations)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		viewModel.requestDeleteAssistantConversationsAction()
		testScheduler.advanceUntilIdle()

		assertEquals(0, assistantConversations.deletions)
		viewModel.effect.test {
			assertEquals(About.Effect.NavigateToDeleteAssistantConversationsConfirmation, awaitItem())

			cancelAndIgnoreRemainingEvents()
		}
	}

	private fun createViewModel(
		testScheduler: TestCoroutineScheduler,
		assistantAvailability: FakeAssistantAvailabilityRepository = FakeAssistantAvailabilityRepository(),
		assistantConsent: FakeAssistantConsentRepository = FakeAssistantConsentRepository(),
		assistantConversations: FakeAssistantConversationsRepository = FakeAssistantConversationsRepository()
	): AboutViewModel {
		return AboutViewModel(
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
	): AboutMachine {
		return AboutMachine(
			loadVersionUseCase = LoadVersionUseCase(
				aboutRepository = FakeAboutRepository(),
				reportingRepository = RecordingReportingRepository()
			),
			sendSupportEmailUseCase = SendSupportEmailUseCase(
				configRepository = FakeConfigRepository(),
				deviceInfoRepository = FakeDeviceInfoRepository(),
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
			usageDataConsentRepository = InMemoryUsageDataConsentRepository(),
			observeAssistantAvailabilityUseCase = ObserveAssistantAvailabilityUseCase(
				assistantAvailabilityRepository = assistantAvailability,
				reportingRepository = RecordingReportingRepository()
			),
			observeAssistantConsentUseCase = ObserveAssistantConsentUseCase(
				assistantConsentRepository = assistantConsent,
				reportingRepository = RecordingReportingRepository()
			),
			setAssistantConsentUseCase = SetAssistantConsentUseCase(
				assistantConsentRepository = assistantConsent,
				reportingRepository = RecordingReportingRepository()
			),
			deleteAssistantConversationsUseCase = DeleteAssistantConversationsUseCase(
				assistantConversationsRepository = assistantConversations,
				reportingRepository = RecordingReportingRepository()
			)
		)
	}
}
