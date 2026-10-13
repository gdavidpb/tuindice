@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.presentation.transition

import com.gdavidpb.tuindice.base.data.source.InMemoryEvaluationsWeekFocusDataSource
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
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.testing.InMemoryEvaluationsSelectionRepository
import com.gdavidpb.tuindice.evaluations.testing.ReadyRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingSyncStatusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
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

	private fun TestScope.definitionWith() = EvaluationsMachine(
		getEvaluationsUseCase = withReporting { reporting ->
			GetEvaluationsUseCase(
				evaluationRepository = RecordingEvaluationRepository(),
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
		weekFocusRepository = InMemoryEvaluationsWeekFocusDataSource(),
		clock = Clock.System
	).define(
		object : MachineHost<Evaluations.Effect> {
			override fun sendEffect(effect: Evaluations.Effect) = Unit

			override fun processInternalEvent(event: Any) = Unit

			override fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job {
				return launch(block = block)
			}
		}
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

	private fun content(
		selected: EvaluationsWeekKey,
		weekOneStart: LocalDate?
	) = Evaluations.State.Content(
		weekItem = weeks.first { item -> item.key == selected },
		weekItems = weeks,
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

	private companion object {
		val FIRST_ANCHOR = LocalDate(2026, 9, 28)
		val SECOND_ANCHOR = LocalDate(2026, 8, 31)
	}
}
