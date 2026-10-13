@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.presentation.transition

import com.gdavidpb.tuindice.base.data.source.InMemoryEvaluationsWeekFocusDataSource
import com.gdavidpb.tuindice.base.domain.repository.EvaluationsWeekFocusRepository
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionResult
import com.gdavidpb.tuindice.evaluations.domain.usecase.EnsureEvaluationsLoadedUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.SetSelectedWeekUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsInternalEvent
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsMachine
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsExplanation
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsIllustration
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.testing.InMemoryEvaluationsSelectionRepository
import com.gdavidpb.tuindice.evaluations.testing.ReadyRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingSyncStatusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * The rows that keep the selected week honest, one at a time and without the view model (whose
 * contract test does not run on Android host): the selection survives a new observation of the
 * same anchor, is replaced by the default when the Monday that opens week 1 moves, and is saved
 * together with that Monday.
 */
class EvaluationsWeekTransitionsTest {
	private val selectionRepository = InMemoryEvaluationsSelectionRepository()
	private val weekFocus = InMemoryEvaluationsWeekFocusDataSource()

	private fun TestScope.definitionWith() = machine().define(host(scope = this))

	private fun host(scope: CoroutineScope) = object : MachineHost<Evaluations.Effect> {
		override fun sendEffect(effect: Evaluations.Effect) = Unit

		override fun processInternalEvent(event: Any) = Unit

		override fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job {
			return scope.launch(block = block)
		}
	}

	private fun machine(
		weekFocusRepository: EvaluationsWeekFocusRepository = weekFocus,
		observedRepository: RecordingEvaluationRepository = RecordingEvaluationRepository()
	) = EvaluationsMachine(
		getEvaluationsUseCase = withReporting { reporting ->
			GetEvaluationsUseCase(
				evaluationRepository = observedRepository,
				recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
				syncStatusRepository = RecordingSyncStatusRepository(),
				evaluationsSelectionRepository = selectionRepository,
				reportingRepository = reporting,
				clock = Clock.System
			)
		},
		ensureEvaluationsLoadedUseCase = withReporting { reporting ->
			EnsureEvaluationsLoadedUseCase(RecordingEvaluationRepository(), reporting)
		},
		updateEvaluationsUseCase = withReporting { reporting ->
			UpdateEvaluationsUseCase(RecordingEvaluationRepository(), reporting)
		},
		getEvaluationUseCase = withReporting { reporting ->
			GetEvaluationUseCase(RecordingEvaluationRepository(), reporting)
		},
		updateEvaluationUseCase = withReporting { reporting ->
			UpdateEvaluationUseCase(RecordingEvaluationRepository(), reporting, UpdateEvaluationExceptionHandler())
		},
		removeEvaluationUseCase = withReporting { reporting ->
			RemoveEvaluationUseCase(RecordingEvaluationRepository(), reporting, RemoveEvaluationExceptionHandler())
		},
		setSelectedWeekUseCase = SetSelectedWeekUseCase(
			evaluationsSelectionRepository = selectionRepository,
			reportingRepository = RecordingReportingRepository()
		),
		weekFocusRepository = weekFocusRepository,
		clock = Clock.System
	)

	private fun <T> withReporting(build: (RecordingReportingRepository) -> T): T {
		return build(RecordingReportingRepository())
	}

	private fun week(key: EvaluationsWeekKey) = EvaluationsWeekItem(
		key = key,
		labelText = key.toString(),
		days = emptyList()
	)

	private val weeks = listOf(
		week(EvaluationsWeekKey.Academic(1)),
		week(EvaluationsWeekKey.Academic(3)),
		week(EvaluationsWeekKey.Academic(5))
	)

	private val allWeeks = (1..12).map { number -> week(EvaluationsWeekKey.Academic(number)) }

	private fun content(
		selected: EvaluationsWeekKey,
		weekOneStart: LocalDate?,
		weekItems: List<EvaluationsWeekItem> = weeks
	) = Evaluations.State.Content(
		weekItem = weekItems.first { item -> item.key == selected },
		weekItems = weekItems,
		selectedWeekKey = selected,
		evaluationGroups = emptyList(),
		evaluationWeekGroups = emptyList(),
		weekOneStart = weekOneStart
	)

	private fun observed(
		defaultWeekKey: EvaluationsWeekKey,
		weekOneStart: LocalDate?
	) = EvaluationsInternalEvent.EvaluationsContentObserved(
		weekItems = weeks,
		defaultWeekKey = defaultWeekKey,
		evaluationWeekGroups = emptyList(),
		weekOneStart = weekOneStart
	)

	private suspend fun Evaluations.State.after(
		definition: MachineDefinition<Evaluations.State>,
		event: Any
	): Evaluations.State {
		val result = definition.process(this, event)

		assertIs<TransitionResult.Transitioned<Evaluations.State>>(result)

		return result.toState
	}

	@Test
	fun aNewObservationOfTheSameAnchor_keepsTheWeekTheUserPicked() = runTest {
		val definition = definitionWith()
		val state = content(selected = EvaluationsWeekKey.Academic(5), weekOneStart = FIRST_ANCHOR)

		val after = state.after(
			definition,
			observed(defaultWeekKey = EvaluationsWeekKey.Academic(3), weekOneStart = FIRST_ANCHOR)
		)

		assertEquals(EvaluationsWeekKey.Academic(5), assertIs<Evaluations.State.Content>(after).selectedWeekKey)
	}

	@Test
	fun whenTheAnchorMoves_theNumberedWeekIsReplacedByTheDefault() = runTest {
		val definition = definitionWith()
		val state = content(selected = EvaluationsWeekKey.Academic(5), weekOneStart = FIRST_ANCHOR)

		val after = state.after(
			definition,
			observed(defaultWeekKey = EvaluationsWeekKey.Academic(3), weekOneStart = SECOND_ANCHOR)
		)

		val reset = assertIs<Evaluations.State.Content>(after)
		assertEquals(EvaluationsWeekKey.Academic(3), reset.selectedWeekKey)
		assertEquals(EvaluationsWeekKey.Academic(3), reset.weekItem.key)
		assertEquals(SECOND_ANCHOR, reset.weekOneStart)
	}

	@Test
	fun whenTheAnchorAppearsForTheFirstTime_theNumberedWeekIsReplacedByTheDefault() = runTest {
		val definition = definitionWith()
		val state = content(selected = EvaluationsWeekKey.Academic(5), weekOneStart = null)

		val after = state.after(
			definition,
			observed(defaultWeekKey = EvaluationsWeekKey.Academic(1), weekOneStart = SECOND_ANCHOR)
		)

		assertEquals(EvaluationsWeekKey.Academic(1), assertIs<Evaluations.State.Content>(after).selectedWeekKey)
	}

	@Test
	fun whenTheAnchorMoves_continuousIsKept() = runTest {
		val continuous = week(EvaluationsWeekKey.Continuous)
		val definition = definitionWith()
		val state = Evaluations.State.Content(
			weekItem = continuous,
			weekItems = weeks + continuous,
			selectedWeekKey = EvaluationsWeekKey.Continuous,
			evaluationGroups = emptyList(),
			evaluationWeekGroups = emptyList(),
			weekOneStart = FIRST_ANCHOR
		)

		val after = state.after(
			definition,
			EvaluationsInternalEvent.EvaluationsContentObserved(
				weekItems = weeks + continuous,
				defaultWeekKey = EvaluationsWeekKey.Academic(3),
				evaluationWeekGroups = emptyList(),
				weekOneStart = SECOND_ANCHOR
			)
		)

		assertEquals(EvaluationsWeekKey.Continuous, assertIs<Evaluations.State.Content>(after).selectedWeekKey)
	}

	@Test
	fun selectingAWeek_savesItWithTheAnchorOfTheContentOnScreen() = runTest {
		val definition = definitionWith()
		val state = content(selected = EvaluationsWeekKey.Academic(1), weekOneStart = FIRST_ANCHOR)

		val after = state.after(definition, Evaluations.Action.SelectWeek(EvaluationsWeekKey.Academic(5)))
		advanceUntilIdle()

		assertEquals(EvaluationsWeekKey.Academic(5), assertIs<Evaluations.State.Content>(after).selectedWeekKey)
		assertEquals("academic:2026-09-28:5", selectionRepository.observeSelectedWeekKey().first())
	}

	@Test
	fun selectingAnAcademicWeekWithoutAnAnchor_savesNothing() = runTest {
		val definition = definitionWith()
		val state = content(selected = EvaluationsWeekKey.Academic(1), weekOneStart = null)

		state.after(definition, Evaluations.Action.SelectWeek(EvaluationsWeekKey.Academic(5)))
		advanceUntilIdle()

		assertNull(selectionRepository.observeSelectedWeekKey().first())
	}

	@Test
	fun aWeekFocus_selectsTheWeekTheListFilesTheMondayUnder_heldToTheWeeksOfTheTerm() = runTest {
		val definition = definitionWith()

		// Monday of week 1 is September 28th: a Monday before it is filed under week 1, a Monday after
		// week 12 (13 here) under week 12, like the evaluations of those days are listed.
		listOf(
			LocalDate(2026, 10, 26) to 5,
			LocalDate(2026, 9, 21) to 1,
			LocalDate(2026, 12, 14) to 12,
			LocalDate(2026, 12, 21) to 12,
			LocalDate(2027, 3, 1) to 12
		).forEach { (monday, expectedWeek) ->
			val state = content(
				selected = EvaluationsWeekKey.Academic(10),
				weekOneStart = FIRST_ANCHOR,
				weekItems = allWeeks
			)
			weekFocus.request(monday)

			val after = state.after(definition, EvaluationsInternalEvent.WeekFocusRequested(monday))

			val focused = assertIs<Evaluations.State.Content>(after)
			assertEquals(EvaluationsWeekKey.Academic(expectedWeek), focused.selectedWeekKey, "for $monday")
			assertEquals(EvaluationsWeekKey.Academic(expectedWeek), focused.weekItem.key, "for $monday")
			assertNull(weekFocus.pendingWeekStart.value, "left pending: $monday")
		}
	}

	@Test
	fun aWeekFocus_withoutAnAnchor_isTakenWithoutMovingTheSelection() = runTest {
		val definition = definitionWith()
		val monday = LocalDate(2026, 10, 26)
		val state = content(selected = EvaluationsWeekKey.Academic(3), weekOneStart = null)
		weekFocus.request(monday)

		val after = state.after(definition, EvaluationsInternalEvent.WeekFocusRequested(monday))

		assertEquals(state, after)
		assertNull(weekFocus.pendingWeekStart.value)
	}

	@Test
	fun aWeekFocus_isNotSavedAsTheWeekTheUserPicked_butASelectionIs() = runTest {
		val definition = definitionWith()
		val monday = LocalDate(2026, 10, 26)
		val state = content(
			selected = EvaluationsWeekKey.Academic(1),
			weekOneStart = FIRST_ANCHOR,
			weekItems = allWeeks
		)
		weekFocus.request(monday)

		val focused = state.after(definition, EvaluationsInternalEvent.WeekFocusRequested(monday))
		advanceUntilIdle()

		// Navigation is not a preference: the next opening of the tab follows the current week again.
		assertEquals(EvaluationsWeekKey.Academic(5), assertIs<Evaluations.State.Content>(focused).selectedWeekKey)
		assertNull(selectionRepository.observeSelectedWeekKey().first())

		focused.after(definition, Evaluations.Action.SelectWeek(EvaluationsWeekKey.Academic(7)))
		advanceUntilIdle()

		assertEquals("academic:2026-09-28:7", selectionRepository.observeSelectedWeekKey().first())
	}

	@Test
	fun aWeekFocus_staysPendingWhileTheScreenIsOnItsWay_andIsDroppedWhereNothingWillShowIt() = runTest {
		val definition = definitionWith()
		val monday = LocalDate(2026, 10, 26)
		val explanation = EvaluationsExplanation(
			title = UiText.Raw("title"),
			message = UiText.Raw("message"),
			illustration = EvaluationsIllustration.entries.first()
		)

		listOf(
			Evaluations.State.Idle,
			Evaluations.State.Loading,
			Evaluations.State.Failed(message = "failed")
		).forEach { state ->
			weekFocus.request(monday)

			assertEquals(state, state.after(definition, EvaluationsInternalEvent.WeekFocusRequested(monday)))
			assertEquals(monday, weekFocus.pendingWeekStart.value, "dropped in $state")
		}

		listOf(
			Evaluations.State.Empty(),
			Evaluations.State.NoAttempts(explanation = explanation),
			Evaluations.State.RecordDataUnavailable(explanation = explanation)
		).forEach { state ->
			weekFocus.request(monday)

			assertEquals(state, state.after(definition, EvaluationsInternalEvent.WeekFocusRequested(monday)))
			assertNull(weekFocus.pendingWeekStart.value, "left pending in $state")
		}
	}

	@Test
	fun startingTheObservationAgain_doesNotAddAnotherCollectorOfTheWeekFocus() = runTest {
		val pending = MutableStateFlow<LocalDate?>(null)
		val countingFocus = object : EvaluationsWeekFocusRepository {
			override val pendingWeekStart: StateFlow<LocalDate?> = pending

			override fun request(weekStart: LocalDate) {
				pending.value = weekStart
			}

			override fun consume(weekStart: LocalDate) = pending.compareAndSet(weekStart, null)
		}
		// What the observation reads never settles, so it stays out of the way of the focus.
		val machine = machine(
			weekFocusRepository = countingFocus,
			observedRepository = RecordingEvaluationRepository(
				evaluationsSnapshotFlow = flow { awaitCancellation() }
			)
		)
		val host = host(scope = backgroundScope)

		// The retry button sends the load again; the focus collector never ends by itself.
		repeat(3) { machine.startObservation(host) }
		runCurrent()

		assertEquals(1, pending.subscriptionCount.value)
	}

	private companion object {
		val FIRST_ANCHOR = LocalDate(2026, 9, 28)
		val SECOND_ANCHOR = LocalDate(2026, 8, 31)
	}
}
