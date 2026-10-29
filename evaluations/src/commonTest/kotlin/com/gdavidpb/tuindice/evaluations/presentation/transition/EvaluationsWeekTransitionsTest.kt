@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.presentation.transition

import com.gdavidpb.tuindice.base.data.source.InMemoryEvaluationsWeekFocusDataSource
import com.gdavidpb.tuindice.base.domain.model.EvaluationsWeekFocusRequest
import com.gdavidpb.tuindice.base.domain.repository.EvaluationsWeekFocusRepository
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionResult
import com.gdavidpb.tuindice.evaluations.data.source.SelectedWeekWriter
import com.gdavidpb.tuindice.evaluations.domain.model.SelectedWeekPick
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationsSelectionRepository
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
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsIllustration
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.testing.FakeSettings
import com.gdavidpb.tuindice.evaluations.testing.InMemoryEvaluationsSelectionRepository
import com.gdavidpb.tuindice.evaluations.testing.ReadyRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingSyncStatusRepository
import com.gdavidpb.tuindice.evaluations.testing.evaluationItemFixture
import com.gdavidpb.tuindice.evaluations.utils.PreferencesKeys
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
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
@OptIn(ExperimentalCoroutinesApi::class)
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
		observedRepository: RecordingEvaluationRepository = RecordingEvaluationRepository(),
		selectionRepository: EvaluationsSelectionRepository = this.selectionRepository
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
			reportingRepository = RecordingReportingRepository(),
			sessionCoroutineScope = testSessionCoroutineScope()
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

	@Test
	fun aWeekFocusNamingAnEvaluationOfThatWeek_arrivesWithIt() = runTest {
		val definition = definitionWith()
		val monday = LocalDate(2026, 10, 26)
		val state = weekListing(evaluationId = "evaluation-5")
		weekFocus.request(monday, evaluationId = "evaluation-5")

		val focused = state.after(
			definition,
			EvaluationsInternalEvent.WeekFocusRequested(EvaluationsWeekFocusRequest(monday, "evaluation-5"))
		)

		val content = assertIs<Evaluations.State.Content>(focused)
		assertEquals(EvaluationsWeekKey.Academic(5), content.selectedWeekKey)
		assertEquals("evaluation-5", content.arrivalEvaluationId)
	}

	@Test
	fun aWeekFocusNamingAnEvaluationNotListedInThatWeek_opensTheWeekWithoutArrival() = runTest {
		val definition = definitionWith()
		val monday = LocalDate(2026, 10, 26)
		val state = weekListing(evaluationId = "evaluation-5")
		weekFocus.request(monday, evaluationId = "evaluation-9")

		val focused = state.after(
			definition,
			EvaluationsInternalEvent.WeekFocusRequested(EvaluationsWeekFocusRequest(monday, "evaluation-9"))
		)

		val content = assertIs<Evaluations.State.Content>(focused)
		assertEquals(EvaluationsWeekKey.Academic(5), content.selectedWeekKey)
		assertNull(content.arrivalEvaluationId)
	}

	@Test
	fun theListTakingItsArrival_clearsOnlyThatEvaluation() = runTest {
		val definition = definitionWith()
		val arrived = weekListing(evaluationId = "evaluation-5").copy(arrivalEvaluationId = "evaluation-5")

		val otherTaken = arrived.after(definition, Evaluations.Action.EvaluationArrivalTaken("evaluation-7"))
		assertEquals("evaluation-5", assertIs<Evaluations.State.Content>(otherTaken).arrivalEvaluationId)

		val taken = arrived.after(definition, Evaluations.Action.EvaluationArrivalTaken("evaluation-5"))
		assertNull(assertIs<Evaluations.State.Content>(taken).arrivalEvaluationId)
	}

	private fun weekFocusEvent(weekStart: LocalDate) =
		EvaluationsInternalEvent.WeekFocusRequested(EvaluationsWeekFocusRequest(weekStart))

	// Content that lists one evaluation under week 5, with the selection on week 1.
	private fun weekListing(evaluationId: String) = content(
		selected = EvaluationsWeekKey.Academic(1),
		weekOneStart = FIRST_ANCHOR,
		weekItems = allWeeks
	).copy(
		evaluationWeekGroups = listOf(
			EvaluationsWeekGroupItem(
				key = EvaluationsWeekKey.Academic(5),
				title = "Semana 5",
				groups = listOf(EvaluationsGroupItem(items = listOf(evaluationItemFixture(evaluationId = evaluationId))))
			)
		)
	)

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

	// The student picks week 3 and then week 5 while the save of week 3 is still on its way. That first save is held
	// before it reaches the writer, so the save of week 5 lands first and the held one lands last: the week left saved
	// is the last pick made, whatever the order the saves land in.
	@Test
	fun twoPicksInARow_leaveTheLastOneSaved() = runTest {
		val store = HeldFirstSaveSelectionRepository()
		val definition = machine(selectionRepository = store).define(host(scope = this))
		val state = content(selected = EvaluationsWeekKey.Academic(1), weekOneStart = FIRST_ANCHOR)
		val pickWeek3 = Evaluations.Action.SelectWeek(EvaluationsWeekKey.Academic(3))
		val pickWeek5 = Evaluations.Action.SelectWeek(EvaluationsWeekKey.Academic(5))

		val afterFirstPick = state.after(definition, pickWeek3)
		advanceUntilIdle()

		val afterSecondPick = afterFirstPick.after(definition, pickWeek5)
		store.releaseHeldSave()
		advanceUntilIdle()

		assertEquals(EvaluationsWeekKey.Academic(5), assertIs<Evaluations.State.Content>(afterSecondPick).selectedWeekKey)
		assertEquals("academic:2026-09-28:5", store.savedWeekKey)
	}

	// Two machines, one per screen, save through the same store: the last pick made is the one left saved, whichever
	// machine made it. The first save is held before it reaches the writer, so the second pick lands before it.
	@Test
	fun twoMachinesOverOneStore_theLastPickIsTheOneSaved() = runTest {
		val store = HeldFirstSaveSelectionRepository()
		val firstScreen = machine(selectionRepository = store).define(host(scope = this))
		val secondScreen = machine(selectionRepository = store).define(host(scope = this))
		val state = content(selected = EvaluationsWeekKey.Academic(1), weekOneStart = FIRST_ANCHOR)

		state.after(firstScreen, Evaluations.Action.SelectWeek(EvaluationsWeekKey.Academic(3)))
		advanceUntilIdle()

		state.after(secondScreen, Evaluations.Action.SelectWeek(EvaluationsWeekKey.Academic(5)))
		store.releaseHeldSave()
		advanceUntilIdle()

		assertEquals("academic:2026-09-28:5", store.savedWeekKey)
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

			val after = state.after(definition, weekFocusEvent(monday))

			val focused = assertIs<Evaluations.State.Content>(after)
			assertEquals(EvaluationsWeekKey.Academic(expectedWeek), focused.selectedWeekKey, "for $monday")
			assertEquals(EvaluationsWeekKey.Academic(expectedWeek), focused.weekItem.key, "for $monday")
			assertNull(weekFocus.pending.value, "left pending: $monday")
		}
	}

	@Test
	fun aWeekFocus_withoutAnAnchor_isTakenWithoutMovingTheSelection() = runTest {
		val definition = definitionWith()
		val monday = LocalDate(2026, 10, 26)
		val state = content(selected = EvaluationsWeekKey.Academic(3), weekOneStart = null)
		weekFocus.request(monday)

		val after = state.after(definition, weekFocusEvent(monday))

		assertEquals(state, after)
		assertNull(weekFocus.pending.value)
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

		val focused = state.after(definition, weekFocusEvent(monday))
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

			assertEquals(state, state.after(definition, weekFocusEvent(monday)))
			assertEquals(EvaluationsWeekFocusRequest(monday), weekFocus.pending.value, "dropped in $state")
		}

		listOf(
			Evaluations.State.Empty(),
			Evaluations.State.NoAttempts(explanation = explanation),
			Evaluations.State.RecordDataUnavailable(explanation = explanation)
		).forEach { state ->
			weekFocus.request(monday)

			assertEquals(state, state.after(definition, weekFocusEvent(monday)))
			assertNull(weekFocus.pending.value, "left pending in $state")
		}
	}

	@Test
	fun startingTheObservationAgain_doesNotAddAnotherCollectorOfTheWeekFocus() = runTest {
		val requests = MutableStateFlow<EvaluationsWeekFocusRequest?>(null)
		val countingFocus = object : EvaluationsWeekFocusRepository {
			override val pending: StateFlow<EvaluationsWeekFocusRequest?> = requests

			override fun request(weekStart: LocalDate, evaluationId: String?) {
				requests.value = EvaluationsWeekFocusRequest(weekStart = weekStart, evaluationId = evaluationId)
			}

			override fun consume(request: EvaluationsWeekFocusRequest) = requests.compareAndSet(request, null)
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

		assertEquals(1, requests.subscriptionCount.value)
	}

	// A selection store whose first save waits until the test releases it, as a slow store does. The week it keeps
	// is the one of the last pick made, whichever save lands last.
	private class HeldFirstSaveSelectionRepository : EvaluationsSelectionRepository {
		private val heldSave = CompletableDeferred<Unit>()
		private var isFirstSaveSeen = false

		private val settings = FakeSettings()

		// The saves go through the writer the data source uses, so the double exercises the real order of the saves.
		private val selectedWeekWriter = SelectedWeekWriter(settings = settings)

		val savedWeekKey: String?
			get() = settings.getStringOrNull(PreferencesKeys.SELECTED_EVALUATIONS_WEEK)

		override fun observeSelectedWeekKey(): Flow<String?> = selectedWeekWriter.observeSelectedWeekKey()

		override suspend fun saveSelectedWeekPick(pick: SelectedWeekPick) {
			if (!isFirstSaveSeen) {
				isFirstSaveSeen = true
				heldSave.await()
			}

			selectedWeekWriter.save(pick = pick)
		}

		fun releaseHeldSave() {
			heldSave.complete(Unit)
		}
	}

	private companion object {
		val FIRST_ANCHOR = LocalDate(2026, 9, 28)
		val SECOND_ANCHOR = LocalDate(2026, 8, 31)
	}
}
