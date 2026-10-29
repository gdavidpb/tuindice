package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionResult
import com.gdavidpb.tuindice.summary.domain.usecase.GetCameraAvailabilityUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveSyncUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.SummaryObservations
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryInternalEvent
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCareerItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummarySyncItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermReportItem
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.createSummaryTermReports
import com.gdavidpb.tuindice.summary.testing.summaryContentState
import com.gdavidpb.tuindice.summary.testing.summaryHubItem
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// The hub is a field of every state and has its own observation, which speaks once: whatever else
// happens to the screen, a state that changes must carry the one it had.
class SummaryHubTransitionsTest {
	private val hub = summaryHubItem()
	private val definition: MachineDefinition<Summary.State> = createDefinition()

	private val states: List<Summary.State> = listOf(
		Summary.State.Idle(hub = hub),
		Summary.State.Loading(isUserRefreshing = true, hub = hub),
		Summary.State.Failed(hub = hub),
		summaryContentState(hub = hub)
	)

	// What can reach the machine without a file: every action and internal event but the upload.
	private val events: List<Any> = listOf(
		Summary.Action.ObserveSummary,
		Summary.Action.RefreshSummary,
		Summary.Action.Resumed,
		Summary.Action.TakeProfilePicture,
		Summary.Action.PickProfilePicture,
		Summary.Action.OpenProfilePictureSettings,
		Summary.Action.RemoveProfilePicture,
		Summary.Action.ConfirmRemoveProfilePicture,
		Summary.Action.OpenTarget(SummaryHubTarget.Record),
		SummaryInternalEvent.UserObserved(content = summaryContentState()),
		SummaryInternalEvent.ObservationFailed(message = "failed"),
		SummaryInternalEvent.SyncObserved(sync = SummarySyncItem()),
		SummaryInternalEvent.CameraAvailabilityResolved(isCameraAvailable = true),
		SummaryInternalEvent.RefreshSucceeded,
		SummaryInternalEvent.RefreshFailed(message = "failed"),
		SummaryInternalEvent.ProfilePictureUploadStarted(previewPath = "preview"),
		SummaryInternalEvent.ProfilePictureUploadSucceeded(message = "done"),
		SummaryInternalEvent.ProfilePictureUploadFailed(message = "failed"),
		SummaryInternalEvent.ProfilePictureRemovalStarted,
		SummaryInternalEvent.ProfilePictureRemovalSucceeded(message = "done"),
		SummaryInternalEvent.ProfilePictureRemovalFailed(message = "failed", clearProfilePicture = false)
	)

	@Test
	fun noEvent_butTheHubObserved_changesTheHubOfAState() = runTest {
		var accepted = 0

		for (state in states) {
			for (event in events) {
				val result = definition.process(state, event)

				if (result is TransitionResult.Transitioned<Summary.State>) {
					accepted++
					assertEquals(
						hub,
						result.toState.hub,
						"${event::class.simpleName} in ${state::class.simpleName} must keep the hub"
					)
				}
			}
		}

		// The table is not empty of rows for these pairs: the check above looked at something.
		assertTrue(accepted >= states.size * 6, "only $accepted of the pairs were accepted")
	}

	// The line of an unread term report is read apart from the hub: a reading of the hub keeps the line the state has.
	@Test
	fun theHubObserved_keepsTheTermReportLineOfTheState() = runTest {
		val line = SummaryTermReportItem(
			insightId = "term-1:2",
			title = UiText.Raw("Tu informe de Abr - Jul 2026"),
			subtitle = "Cerraste con 4,18",
			target = SummaryHubTarget.TermReport(insightId = "term-1:2")
		)
		val withLine = summaryContentState(hub = hub.copy(termReport = line))
		val reading = SummaryInternalEvent.HubObserved(hub = hub)

		val next = assertIs<TransitionResult.Transitioned<Summary.State>>(
			definition.process(withLine, reading)
		).toState

		assertEquals(line, next.hub.termReport)
	}

	// The line of the term reports is put and taken by its own entry, in every state; the rest of the hub stays as it is.
	@Test
	fun theTermReportEntryObserved_putsAndTakesTheLine_inEveryState() = runTest {
		val line = SummaryTermReportItem(
			insightId = "term-1:2",
			title = UiText.Raw("Tu informe de Abr - Jul 2026"),
			subtitle = "Cerraste con 4,18",
			target = SummaryHubTarget.TermReport(insightId = "term-1:2")
		)

		for (state in states) {
			val withLine = assertIs<TransitionResult.Transitioned<Summary.State>>(
				definition.process(state, SummaryInternalEvent.TermReportEntryObserved(entry = line))
			).toState

			assertEquals(hub.copy(termReport = line), withLine.hub, "the line put in ${state::class.simpleName}")

			val withoutLine = assertIs<TransitionResult.Transitioned<Summary.State>>(
				definition.process(withLine, SummaryInternalEvent.TermReportEntryObserved(entry = null))
			).toState

			assertEquals(hub, withoutLine.hub, "the line taken in ${state::class.simpleName}")
		}
	}

	@Test
	fun theUserObserved_fromAnyStateButTheContent_makesTheContentWithTheHubOfTheState() = runTest {
		val user = SummaryInternalEvent.UserObserved(content = summaryContentState())

		for (state in states.filter { it !is Summary.State.Content }) {
			val result = definition.process(state, user)

			val content = assertIs<Summary.State.Content>(
				assertIs<TransitionResult.Transitioned<Summary.State>>(result).toState
			)

			assertEquals(hub, content.hub, "the user observed in ${state::class.simpleName} must keep the hub")
			assertNotNull(content.hub.week)
		}
	}

	@Test
	fun theRefreshFailing_fromAnyState_leavesTheHubOfTheState() = runTest {
		val failure = SummaryInternalEvent.RefreshFailed(message = "failed")

		for (state in states) {
			val next = assertIs<TransitionResult.Transitioned<Summary.State>>(
				definition.process(state, failure)
			).toState

			assertEquals(hub, next.hub, "a refresh that fails in ${state::class.simpleName} must keep the hub")
		}
	}

	@Test
	fun theObservationFailing_fromAnyState_leavesTheHubOfTheState() = runTest {
		val failure = SummaryInternalEvent.ObservationFailed(message = "failed")

		for (state in states) {
			val next = assertIs<TransitionResult.Transitioned<Summary.State>>(
				definition.process(state, failure)
			).toState

			assertEquals(hub, next.hub, "an observation that fails in ${state::class.simpleName} must keep the hub")
		}
	}

	@Test
	fun theRefreshAsked_fromIdleAndFailed_goesToLoadingWithTheHub() = runTest {
		for (state in states.filter { it is Summary.State.Idle || it is Summary.State.Failed }) {
			val next = assertIs<TransitionResult.Transitioned<Summary.State>>(
				definition.process(state, Summary.Action.RefreshSummary)
			).toState

			assertIs<Summary.State.Loading>(next)
			assertEquals(hub, next.hub, "the refresh asked in ${state::class.simpleName} must keep the hub")
		}
	}

	@Test
	fun theHubObserved_replacesTheHubOfEveryState_andKeepsTheState() = runTest {
		val newer = SummaryHubItem(career = SummaryCareerItem.Invite)

		for (state in states) {
			val next = assertIs<TransitionResult.Transitioned<Summary.State>>(
				definition.process(state, SummaryInternalEvent.HubObserved(hub = newer))
			).toState

			assertEquals(newer, next.hub)
			assertEquals(state::class, next::class)
		}
	}

	private fun createDefinition(): MachineDefinition<Summary.State> {
		val reporting = RecordingReportingRepository()
		val userRepository = RecordingUserRepository()
		val network = FakeNetworkRepository(isAvailable = true)
		val machine = SummaryMachine(
			observations = SummaryObservations(
				user = ObserveUserUseCase(userRepository, reporting),
				sync = ObserveSyncUseCase(FakeSyncStatusRepository(), FakeSyncRepository(), reporting),
				hub = createObserveSummaryHubUseCase()
			),
			getCameraAvailabilityUseCase = GetCameraAvailabilityUseCase(FakeDeviceInfoRepository(), reporting),
			updateUserUseCase = UpdateUserUseCase(
				userRepository = userRepository,
				reportingRepository = reporting,
				exceptionHandler = UpdateUserExceptionHandler(network)
			),
			uploadProfilePictureUseCase = UploadProfilePictureUseCase(
				userRepository = userRepository,
				reportingRepository = reporting,
				exceptionHandler = UploadProfilePictureExceptionHandler(network)
			),
			termReports = createSummaryTermReports(),
			removeProfilePictureUseCase = RemoveProfilePictureUseCase(
				userRepository = userRepository,
				reportingRepository = reporting,
				exceptionHandler = RemoveProfilePictureExceptionHandler(network)
			)
		)

		// Pure: the jobs the transitions ask for are never run, only the table is looked at.
		val host = object : MachineHost<Summary.Effect> {
			override fun sendEffect(effect: Summary.Effect) = Unit

			override fun processInternalEvent(event: Any) = Unit

			override fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job {
				return Job().apply { complete() }
			}
		}

		return machine.define(host)
	}
}
