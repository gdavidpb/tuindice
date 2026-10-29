package com.gdavidpb.tuindice.about.presentation.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.cash.turbine.test
import com.gdavidpb.tuindice.about.domain.usecase.LoadVersionNameUseCase
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
import com.gdavidpb.tuindice.about.testing.FakeAssistantAvailabilityRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantConsentRepository
import com.gdavidpb.tuindice.about.testing.FakeAssistantConversationsRepository
import com.gdavidpb.tuindice.about.testing.FakeSettingsTextProvider
import com.gdavidpb.tuindice.about.testing.FakeStoreUrlDataSource
import com.gdavidpb.tuindice.about.testing.fakeAssistantConsentWriteQueue
import com.gdavidpb.tuindice.about.testing.fakeDeleteAssistantConversationsUseCase
import com.gdavidpb.tuindice.about.testing.fakeObserveProfileIdentityUseCase
import com.gdavidpb.tuindice.about.testing.fakeRefreshAssistantConsentUseCase
import com.gdavidpb.tuindice.about.testing.fakeSetAssistantConsentUseCase
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.testkit.base.repository.FakeAppEnvironmentRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingBrowserRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

// What the writes of Preferences do when the screen is left in the middle of them, and what they tell the screen.
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsAssistantWritesContractTest {
	@Test
	fun setAssistantConsent_writeEndsEvenWhenPreferencesIsLeftBeforeIt() = runTest {
		val writeGate = CompletableDeferred<Unit>()
		val assistantConsent = FakeAssistantConsentRepository(granted = false, writeGate = writeGate)
		val store = ViewModelStore()
		val viewModel = store.settingsViewModel(testScheduler = testScheduler, assistantConsent = assistantConsent)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		viewModel.dispatchAction(Settings.Action.SetAssistantConsent(granted = true))
		testScheduler.advanceUntilIdle()

		// Leaving Preferences clears its store while the store is still answering the write.
		store.clear()
		writeGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		assertEquals(1, assistantConsent.writes)
		assertTrue(assistantConsent.isAssistantConsentGranted())
	}

	@Test
	fun setAssistantConsent_twoMovesInARow_theStoreEndsWithTheLastOne() = runTest {
		// Only the first write is held. Without the queue, the second write would reach the store before the first one.
		val firstWriteGate = CompletableDeferred<Unit>()
		val assistantConsent = FakeAssistantConsentRepository(
			granted = false,
			writeGate = firstWriteGate,
			holdsOnlyTheFirstWrite = true
		)
		val viewModel = createViewModel(testScheduler = testScheduler, assistantConsent = assistantConsent)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		viewModel.dispatchAction(Settings.Action.SetAssistantConsent(granted = true))
		testScheduler.advanceUntilIdle()
		viewModel.dispatchAction(Settings.Action.SetAssistantConsent(granted = false))
		testScheduler.advanceUntilIdle()

		firstWriteGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		// The store received the moves in the order they were made, so it keeps the last one.
		assertEquals(listOf(true, false), assistantConsent.storedValues)
		assertFalse(assistantConsent.isAssistantConsentGranted())
		assertFalse(assertIs<Settings.State.Content>(viewModel.state.value).assistantConsentGranted)
	}

	@Test
	fun setAssistantConsent_whileAWriteIsPending_theSwitchShowsTheLastMoveOnly() = runTest {
		// Only the first write is held: the second move waits for it, and the store changes while the switch is pending.
		val firstWriteGate = CompletableDeferred<Unit>()
		val assistantConsent = FakeAssistantConsentRepository(
			granted = false,
			writeGate = firstWriteGate,
			holdsOnlyTheFirstWrite = true
		)
		val viewModel = createViewModel(testScheduler = testScheduler, assistantConsent = assistantConsent)

		// Every value the switch takes, in order: a value the switch takes and then drops is recorded too.
		val switchValues = mutableListOf<Boolean?>()
		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
			viewModel.state
				.map { state -> (state as? Settings.State.Content)?.assistantConsentGranted }
				.distinctUntilChanged()
				.collect { switchValue -> switchValues += switchValue }
		}
		testScheduler.advanceUntilIdle()

		viewModel.dispatchAction(Settings.Action.SetAssistantConsent(granted = true))
		testScheduler.advanceUntilIdle()
		viewModel.dispatchAction(Settings.Action.SetAssistantConsent(granted = false))
		testScheduler.advanceUntilIdle()
		switchValues.clear()

		firstWriteGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		// The store changes while the second move is pending: the switch keeps the last move, and once the writes have
		// ended it shows the store's value, which is the value of the second move.
		assertEquals(emptyList(), switchValues)
		assertEquals(listOf(true, false), assistantConsent.storedValues)
		assertFalse(assertIs<Settings.State.Content>(viewModel.state.value).assistantConsentGranted)
	}

	@Test
	fun confirmDeleteAssistantConversations_deletionEndsEvenWhenPreferencesIsLeftBeforeIt() = runTest {
		val deleteGate = CompletableDeferred<Unit>()
		val assistantConversations = FakeAssistantConversationsRepository(deleteGate = deleteGate)
		val store = ViewModelStore()
		val viewModel = store.settingsViewModel(
			testScheduler = testScheduler,
			assistantConversations = assistantConversations
		)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		viewModel.dispatchAction(Settings.Action.ConfirmDeleteAssistantConversations)
		testScheduler.advanceUntilIdle()

		// Leaving Preferences in the middle of the request must not leave the deletion half done.
		store.clear()
		deleteGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		assertEquals(1, assistantConversations.deletions)
	}

	@Test
	fun availabilityThatStaysVisible_readsTheConsentOnlyOnce() = runTest {
		val assistantAvailability = FakeAssistantAvailabilityRepository(visible = true)
		val assistantConsent = FakeAssistantConsentRepository(granted = false, serverGranted = true)
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			assistantAvailability = assistantAvailability,
			assistantConsent = assistantConsent
		)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()
		assertEquals(1, assistantConsent.refreshes)

		// The mode changes while the row stays visible: nothing new is shown, so the consent is not read again.
		assistantAvailability.publish(AssistantAvailability(mode = AssistantMode.GUIDED, serverDisabled = false))
		testScheduler.advanceUntilIdle()

		assertEquals(1, assistantConsent.refreshes)
	}

	@Test
	fun setAssistantConsent_aScreenOpenedWhileTheWriteIsInFlight_takesTheStoredValueWhenTheWriteEnds() = runTest {
		val writeGate = CompletableDeferred<Unit>()
		val assistantConsent = FakeAssistantConsentRepository(granted = false, writeGate = writeGate)
		// The queue belongs to the app, not to a screen: both screens of Preferences share it and its session.
		val writes = ConsentWrites(testScheduler)

		// The student moves the switch on and the write waits in flight. Then Preferences is left, which clears its screen.
		val leftScreen = ViewModelStore()
		val firstScreen = leftScreen.settingsViewModel(
			testScheduler = testScheduler,
			assistantConsent = assistantConsent,
			writes = writes
		)
		backgroundScope.launchStateCollector(flow = firstScreen.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		firstScreen.dispatchAction(Settings.Action.SetAssistantConsent(granted = true))
		testScheduler.advanceUntilIdle()
		leftScreen.clear()

		// The store changes while the write is still in flight, and Preferences is opened again. The new screen's first
		// look at the store happens while the write is pending, so that look must not be the last word.
		assistantConsent.changeStore(granted = true)

		val returnedScreen = ViewModelStore().settingsViewModel(
			testScheduler = testScheduler,
			assistantConsent = assistantConsent,
			writes = writes
		)
		backgroundScope.launchStateCollector(flow = returnedScreen.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		// The write ends. The store does not emit again, so the new screen learns the value only when the queue drains.
		writeGate.complete(Unit)
		testScheduler.advanceUntilIdle()

		assertTrue(assertIs<Settings.State.Content>(returnedScreen.state.value).assistantConsentGranted)
	}

	@Test
	fun setAssistantConsent_whenTheWriteFails_restoresTheSwitchAndSaysSo() = runTest {
		val assistantConsent = FakeAssistantConsentRepository(granted = false, writesFail = true)
		val viewModel = createViewModel(testScheduler = testScheduler, assistantConsent = assistantConsent)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		viewModel.dispatchAction(Settings.Action.SetAssistantConsent(granted = true))
		testScheduler.advanceUntilIdle()

		// The store kept its value, so the switch goes back to it, and the student is told the choice was not saved.
		assertFalse(assertIs<Settings.State.Content>(viewModel.state.value).assistantConsentGranted)
		viewModel.effect.test {
			assertEquals(Settings.Effect.ShowSnackBar(message = "text:consent-failed"), awaitItem())

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun confirmDeleteAssistantConversations_saysTheConversationsWereDeleted() = runTest {
		val assistantConversations = FakeAssistantConversationsRepository()
		val viewModel = createViewModel(testScheduler = testScheduler, assistantConversations = assistantConversations)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		viewModel.dispatchAction(Settings.Action.ConfirmDeleteAssistantConversations)
		testScheduler.advanceUntilIdle()

		assertEquals(1, assistantConversations.deletions)
		viewModel.effect.test {
			assertEquals(Settings.Effect.ShowSnackBar(message = "text:conversations-deleted"), awaitItem())

			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun confirmDeleteAssistantConversations_whenTheDeletionFails_saysSo() = runTest {
		val viewModel = createViewModel(
			testScheduler = testScheduler,
			assistantConversations = FakeAssistantConversationsRepository(deletesFail = true)
		)
		backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)
		testScheduler.advanceUntilIdle()

		viewModel.dispatchAction(Settings.Action.ConfirmDeleteAssistantConversations)
		testScheduler.advanceUntilIdle()

		viewModel.effect.test {
			assertEquals(Settings.Effect.ShowSnackBar(message = "text:conversations-delete-failed"), awaitItem())

			cancelAndIgnoreRemainingEvents()
		}
	}

	private fun createViewModel(
		testScheduler: TestCoroutineScheduler,
		assistantAvailability: FakeAssistantAvailabilityRepository = FakeAssistantAvailabilityRepository(),
		assistantConsent: FakeAssistantConsentRepository = FakeAssistantConsentRepository(),
		assistantConversations: FakeAssistantConversationsRepository = FakeAssistantConversationsRepository(),
		writes: ConsentWrites = ConsentWrites(testScheduler)
	): SettingsViewModel {
		return SettingsViewModel(
			screenMachine = createMachine(
				assistantAvailability = assistantAvailability,
				assistantConsent = assistantConsent,
				assistantConversations = assistantConversations,
				writes = writes
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = TestTuIndiceDispatchers(UnconfinedTestDispatcher(testScheduler))
		)
	}

	// The view model of the screen, held by a store of its own: clearing the store is leaving Preferences.
	private fun ViewModelStore.settingsViewModel(
		testScheduler: TestCoroutineScheduler,
		assistantConsent: FakeAssistantConsentRepository = FakeAssistantConsentRepository(),
		assistantConversations: FakeAssistantConversationsRepository = FakeAssistantConversationsRepository(),
		writes: ConsentWrites = ConsentWrites(testScheduler)
	): SettingsViewModel {
		return ViewModelProvider.create(
			store = this,
			factory = viewModelFactory {
				initializer<SettingsViewModel> {
					createViewModel(
						testScheduler = testScheduler,
						assistantConsent = assistantConsent,
						assistantConversations = assistantConversations,
						writes = writes
					)
				}
			}
		)[SettingsViewModel::class]
	}

	// The queue of the consent writes and the session they run in: the screens of Preferences share both, as the app does.
	private class ConsentWrites(testScheduler: TestCoroutineScheduler) {
		val session = testSessionCoroutineScope(UnconfinedTestDispatcher(testScheduler))

		val queue = fakeAssistantConsentWriteQueue(sessionCoroutineScope = session)
	}

	private fun createMachine(
		assistantAvailability: FakeAssistantAvailabilityRepository,
		assistantConsent: FakeAssistantConsentRepository,
		assistantConversations: FakeAssistantConversationsRepository,
		writes: ConsentWrites
	): SettingsMachine {
		return SettingsMachine(
			contentUseCases = SettingsContentUseCases(
				loadVersionName = LoadVersionNameUseCase(
					deviceInfoRepository = FakeDeviceInfoRepository(versionName = "1.2.3"),
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
				setConsent = fakeSetAssistantConsentUseCase(
					assistantConsentRepository = assistantConsent,
					sessionCoroutineScope = writes.session
				),
				refreshConsent = fakeRefreshAssistantConsentUseCase(assistantConsent),
				deleteConversations = fakeDeleteAssistantConversationsUseCase(
					assistantConversationsRepository = assistantConversations,
					sessionCoroutineScope = writes.session
				),
				consentWrites = writes.queue
			),
			textProvider = FakeSettingsTextProvider()
		)
	}
}
