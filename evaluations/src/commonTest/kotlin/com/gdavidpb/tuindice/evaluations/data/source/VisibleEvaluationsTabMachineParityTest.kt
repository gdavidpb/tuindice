@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.data.source

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
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsMachine
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.InMemoryEvaluationsSelectionRepository
import com.gdavidpb.tuindice.evaluations.testing.NEXT_TERM
import com.gdavidpb.tuindice.evaluations.testing.NEXT_TERM_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.NEXT_TERM_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.ReadyRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingSyncStatusRepository
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// The parity between the tab and the contract (VisibleEvaluationsRepository), through the real tab
// machine. The machine reads its texts (week labels) from the Compose resources, which the Android
// host does not have, so this class is left out of testAndroidHostTest (the root build file lists
// it) and runs where the resources exist: the iOS simulator tests.
@OptIn(ExperimentalCoroutinesApi::class)
class VisibleEvaluationsTabMachineParityTest {
	// The tab is the real machine: what is compared is what it leaves in its content state after it
	// observed the repository through its use case and mapped what it saw, not a filter applied by hand.
	@Test
	fun observeVisibleEvaluationsFlow_deliversWhatTheTabMachineLeavesInItsContent() = runTest {
		val repository = RecordingEvaluationRepository(
			initialEvaluations = listOf(
				DEFAULT_PENDING_EVALUATION,
				DEFAULT_COMPLETED_EVALUATION,
				NEXT_TERM_EVALUATION,
				DEFAULT_COMPLETED_EVALUATION.copy(id = "orphan", attemptId = "attempt-that-is-gone")
			),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
		)
		val source = VisibleEvaluationsDataSource(evaluationRepository = repository)
		val tab = TabMachine(scope = this, repository = repository)
		val visible = mutableListOf<Set<String>>()

		backgroundScope.launch {
			source.observeVisibleEvaluationsFlow().collect { snapshot ->
				visible += snapshot.value.map { evaluation -> evaluation.id }.toSet()
			}
		}
		tab.start()

		// The same doubles go through four changes; after each one both must agree.
		for (change in listOf<() -> Unit>(
			{},
			{ repository.changeAvailableAttempts(listOf(DEFAULT_EVALUATION_SUBJECT)) },
			{ repository.changeTermAndAttempts(NEXT_TERM, listOf(NEXT_TERM_SUBJECT)) },
			{ repository.changeAvailableAttempts(emptyList()) }
		)) {
			change()
			// The machine reads its texts from the resources, off the test's clock: it is given real
			// time to settle on what the contract delivered (and the assertion is the judge of it).
			settle { tab.listedIds() == visible.last() }

			assertEquals(tab.listedIds(), visible.last())
		}

		assertEquals(
			listOf(
				setOf(DEFAULT_PENDING_EVALUATION.id, DEFAULT_COMPLETED_EVALUATION.id),
				setOf(DEFAULT_PENDING_EVALUATION.id),
				setOf(NEXT_TERM_EVALUATION.id),
				emptySet()
			),
			visible.distinct()
		)
	}

	private suspend fun TestScope.settle(isSettled: () -> Boolean) {
		repeat(SETTLE_ATTEMPTS) {
			runCurrent()
			if (isSettled()) return

			withContext(Dispatchers.Default) { delay(SETTLE_STEP_MILLIS) }
		}
	}

	private companion object {
		const val SETTLE_ATTEMPTS = 200
		const val SETTLE_STEP_MILLIS = 25L
	}
}

// The Evaluations tab as the app runs it, without the view model: the real machine observes the
// repository through its use case, every internal event goes through the real table, and the state
// is whatever the table leaves.
private class TabMachine(
	private val scope: TestScope,
	repository: RecordingEvaluationRepository
) {
	private val selectionRepository = InMemoryEvaluationsSelectionRepository()
	private val machine = EvaluationsMachine(
		getEvaluationsUseCase = GetEvaluationsUseCase(
			evaluationRepository = repository,
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = selectionRepository,
			reportingRepository = RecordingReportingRepository(),
			clock = Clock.System
		),
		ensureEvaluationsLoadedUseCase = EnsureEvaluationsLoadedUseCase(repository, RecordingReportingRepository()),
		updateEvaluationsUseCase = UpdateEvaluationsUseCase(repository, RecordingReportingRepository()),
		getEvaluationUseCase = GetEvaluationUseCase(repository, RecordingReportingRepository()),
		updateEvaluationUseCase = UpdateEvaluationUseCase(
			repository,
			RecordingReportingRepository(),
			UpdateEvaluationExceptionHandler()
		),
		removeEvaluationUseCase = RemoveEvaluationUseCase(
			repository,
			RecordingReportingRepository(),
			RemoveEvaluationExceptionHandler()
		),
		setSelectedWeekUseCase = SetSelectedWeekUseCase(
			evaluationsSelectionRepository = selectionRepository,
			reportingRepository = RecordingReportingRepository(),
			sessionCoroutineScope = testSessionCoroutineScope()
		),
		weekFocusRepository = InMemoryEvaluationsWeekFocusDataSource(),
		clock = Clock.System
	)
	private val host: MachineHost<Evaluations.Effect> = object : MachineHost<Evaluations.Effect> {
		override fun sendEffect(effect: Evaluations.Effect) = Unit

		override fun processInternalEvent(event: Any) {
			scope.backgroundScope.launch {
				val result = definition.process(state, event)

				if (result is TransitionResult.Transitioned<Evaluations.State>) state = result.toState
			}
		}

		override fun launchMachineJob(block: suspend CoroutineScope.() -> Unit): Job {
			return scope.backgroundScope.launch(block = block)
		}
	}
	private val definition: MachineDefinition<Evaluations.State> by lazy { machine.define(host) }

	var state: Evaluations.State = machine.initialState()
		private set

	fun start() {
		machine.startObservation(host)
	}

	// What the screen lists: the evaluations of the content the machine holds, nothing in any other state.
	fun listedIds(): Set<String> {
		val content = state as? Evaluations.State.Content ?: return emptySet()

		return content.evaluationGroups
			.flatMap { group -> group.items }
			.map { item -> item.evaluationId }
			.toSet()
	}
}
