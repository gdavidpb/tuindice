package com.gdavidpb.tuindice.evaluations.presentation.action

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationsExceptionHandler
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.LoadEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.RefreshEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationTypePickerItem
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.ReadyRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EvaluationsActionProcessorContractTest {
	@Test
	fun loadEvaluationsActionProcessor_reducesStateToContent_withWeeklyGroups() = runTest {
		val processor = LoadEvaluationsActionProcessor(
			getEvaluationsUseCase = GetEvaluationsUseCase(
				evaluationRepository = RecordingEvaluationRepository(
					evaluationsFlow = flowOf(
						listOf(
							DEFAULT_PENDING_EVALUATION,
							DEFAULT_COMPLETED_EVALUATION
						)
					),
					initialEvaluations = listOf(
						DEFAULT_PENDING_EVALUATION,
						DEFAULT_COMPLETED_EVALUATION
					)
					),
					recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
					reportingRepository = RecordingReportingRepository()
			)
		)
		val effects = mutableListOf<Evaluations.Effect>()

		processor.process(
			action = Evaluations.Action.LoadEvaluations,
			sideEffect = effects::add
		).test {
			val content = assertIs<Evaluations.State.Content>(awaitItem()(Evaluations.State.Idle))
			assertEquals(2, content.evaluationGroups.flatMap { group -> group.items }.size)
			assertEquals(
				2,
				content.evaluationWeekGroups
					.flatMap { weekGroup -> weekGroup.groups }
					.flatMap { group -> group.items }
					.size
			)
			assertTrue(
				content.evaluationWeekGroups.all { weekGroup ->
					weekGroup.groups.any { group -> group.items.isNotEmpty() }
				}
			)
			awaitComplete()
		}

		assertTrue(effects.isEmpty())
	}

	@Test
	fun loadEvaluationsActionProcessor_placesContinuousEvaluationsInContinuousGroup() = runTest {
		val continuousEvaluation = DEFAULT_PENDING_EVALUATION.copy(
			id = "continuous-evaluation",
			scheduleMode = EvaluationScheduleMode.CONTINUOUS,
			date = DEFAULT_PENDING_EVALUATION.date
		)
		val processor = LoadEvaluationsActionProcessor(
			getEvaluationsUseCase = GetEvaluationsUseCase(
				evaluationRepository = RecordingEvaluationRepository(
					evaluationsFlow = flowOf(
						listOf(
							continuousEvaluation,
							DEFAULT_PENDING_EVALUATION
						)
					),
					initialEvaluations = listOf(
						continuousEvaluation,
						DEFAULT_PENDING_EVALUATION
					)
				),
				recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
				reportingRepository = RecordingReportingRepository()
			)
		)

		processor.process(
			action = Evaluations.Action.LoadEvaluations,
			sideEffect = {}
		).test {
			val content = assertIs<Evaluations.State.Content>(awaitItem()(Evaluations.State.Idle))
			val continuousGroup = content.evaluationWeekGroups.first { group ->
				group.key == EvaluationsWeekKey.Continuous
			}

			assertEquals("Continuas", continuousGroup.title)
			assertEquals(
				listOf(continuousEvaluation.id),
				continuousGroup.groups
					.flatMap { group -> group.items }
					.map { item -> item.evaluationId }
			)
			awaitComplete()
		}
	}

	@Test
	fun loadEvaluationsActionProcessor_skipsTransientEmpty_whenSnapshotMovesFromUnsyncedEmptyToContent() = runTest {
		val processor = LoadEvaluationsActionProcessor(
			getEvaluationsUseCase = GetEvaluationsUseCase(
				evaluationRepository = RecordingEvaluationRepository(
					initialEvaluations = emptyList(),
					evaluationsSnapshotFlow = flowOf(
							ObservedSyncedSnapshot(
								value = emptyList(),
								hasSynced = false
							),
							ObservedSyncedSnapshot(
								value = listOf(DEFAULT_PENDING_EVALUATION),
								hasSynced = true
							)
						),
					availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
				),
				recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
				reportingRepository = RecordingReportingRepository()
			)
		)

		processor.process(
			action = Evaluations.Action.LoadEvaluations,
			sideEffect = {}
		).test {
			assertEquals(Evaluations.State.Loading, awaitItem()(Evaluations.State.Idle))
			val content = assertIs<Evaluations.State.Content>(awaitItem()(Evaluations.State.Loading))
			assertEquals(
				listOf(DEFAULT_PENDING_EVALUATION.id),
				content.evaluationGroups
					.flatMap { group -> group.items }
					.map { item -> item.evaluationId }
			)

			awaitComplete()
		}
	}

	@Test
	fun loadEvaluationsActionProcessor_keepsLoading_whenInitialSnapshotIsEmptyAndNeverSynced() = runTest {
		val processor = LoadEvaluationsActionProcessor(
			getEvaluationsUseCase = GetEvaluationsUseCase(
				evaluationRepository = RecordingEvaluationRepository(
					evaluationsFlow = flowOf(emptyList()),
					hasSyncedEvaluationsFlow = flowOf(false),
					availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
					),
					recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
					reportingRepository = RecordingReportingRepository()
			)
		)

		processor.process(
			action = Evaluations.Action.LoadEvaluations,
			sideEffect = {}
		).test {
			assertEquals(Evaluations.State.Loading, awaitItem()(Evaluations.State.Idle))

			awaitComplete()
		}
	}

	@Test
	fun loadEvaluationsActionProcessor_reducesStateToEmpty_whenInitialSnapshotIsEmptyAfterSync() = runTest {
		val processor = LoadEvaluationsActionProcessor(
			getEvaluationsUseCase = GetEvaluationsUseCase(
				evaluationRepository = RecordingEvaluationRepository(
					evaluationsFlow = flowOf(emptyList()),
					hasSyncedEvaluationsFlow = flowOf(true),
					availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
					),
					recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
					reportingRepository = RecordingReportingRepository()
			)
		)

		processor.process(
			action = Evaluations.Action.LoadEvaluations,
			sideEffect = {}
		).test {
			assertEquals(Evaluations.State.Empty, awaitItem()(Evaluations.State.Idle))

			awaitComplete()
		}
	}

	@Test
	fun loadEvaluationsActionProcessor_reducesStateToNoAttempts_withoutReportingError() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val processor = LoadEvaluationsActionProcessor(
			getEvaluationsUseCase = GetEvaluationsUseCase(
				evaluationRepository = RecordingEvaluationRepository(
					evaluationsFlow = flowOf(emptyList()),
					availableSubjects = emptyList()
					),
					recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
					reportingRepository = reportingRepository
			)
		)

		processor.process(
			action = Evaluations.Action.LoadEvaluations,
			sideEffect = {}
		).test {
			assertEquals(Evaluations.State.NoAttempts, awaitItem()(Evaluations.State.Idle))

			awaitComplete()
		}

		assertTrue(reportingRepository.exceptions.isEmpty())
	}

	@Test
	fun loadEvaluationsActionProcessor_keepsLoading_whenRecordDataIsNotReady() = runTest {
		val processor = LoadEvaluationsActionProcessor(
			getEvaluationsUseCase = GetEvaluationsUseCase(
				evaluationRepository = RecordingEvaluationRepository(
					evaluationsFlow = flowOf(emptyList()),
					availableSubjects = emptyList()
				),
				recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(
					states = flowOf(RecordDataPrerequisiteState(isReady = false, hasFailed = false))
				),
				reportingRepository = RecordingReportingRepository()
			)
		)

		processor.process(
			action = Evaluations.Action.LoadEvaluations,
			sideEffect = {}
		).test {
			assertEquals(Evaluations.State.Loading, awaitItem()(Evaluations.State.Idle))

			awaitComplete()
		}
	}

	@Test
	fun loadEvaluationsActionProcessor_reducesStateToFailed_whenRecordDataFailed() = runTest {
		val processor = LoadEvaluationsActionProcessor(
			getEvaluationsUseCase = GetEvaluationsUseCase(
				evaluationRepository = RecordingEvaluationRepository(),
				recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(
					states = flowOf(RecordDataPrerequisiteState(isReady = false, hasFailed = true))
				),
				reportingRepository = RecordingReportingRepository()
			)
		)

		processor.process(
			action = Evaluations.Action.LoadEvaluations,
			sideEffect = {}
		).test {
			assertEquals(Evaluations.State.Failed, awaitItem()(Evaluations.State.Idle))

			awaitComplete()
		}
	}

	@Test
	fun refreshEvaluationsActionProcessor_keepsEmptyState_whenRefreshing() = runTest {
		val processor = RefreshEvaluationsActionProcessor(
			updateEvaluationsUseCase = UpdateEvaluationsUseCase(
				evaluationRepository = RecordingEvaluationRepository(),
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = UpdateEvaluationsExceptionHandler()
			)
		)

		processor.process(
			action = Evaluations.Action.RefreshEvaluations,
			sideEffect = {}
		).test {
			assertEquals(Evaluations.State.Empty, awaitItem()(Evaluations.State.Empty))

			awaitComplete()
		}
	}

	@Test
	fun refreshEvaluationsActionProcessor_keepsNoAttemptsState_whenRefreshing() = runTest {
		val processor = RefreshEvaluationsActionProcessor(
			updateEvaluationsUseCase = UpdateEvaluationsUseCase(
				evaluationRepository = RecordingEvaluationRepository(),
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = UpdateEvaluationsExceptionHandler()
			)
		)

		processor.process(
			action = Evaluations.Action.RefreshEvaluations,
			sideEffect = {}
		).test {
			assertEquals(Evaluations.State.NoAttempts, awaitItem()(Evaluations.State.NoAttempts))

			awaitComplete()
		}
	}

	@Test
	fun loadEvaluationActionProcessor_reducesStateToEditableContent() = runTest {
		val processor = LoadEvaluationActionProcessor(
			getEvaluationAndAvailableAttemptsUseCase = GetEvaluationAndAvailableAttemptsUseCase(
				RecordingEvaluationRepository(
					initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
					availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
				),
				reportingRepository = RecordingReportingRepository()
			)
		)
		val effects = mutableListOf<Evaluation.Effect>()

		processor.process(
			action = Evaluation.Action.LoadEvaluation(DEFAULT_PENDING_EVALUATION.id),
			sideEffect = effects::add
		).test {
			assertEquals(Evaluation.State.Loading, awaitItem()(Evaluation.State.Failed))

			val content = assertIs<Evaluation.State.Content>(awaitItem()(Evaluation.State.Loading))
			assertEquals(DEFAULT_PENDING_EVALUATION.id, content.evaluationId)
			assertEquals(DEFAULT_EVALUATION_SUBJECT, content.selectedAttempt)
			assertEquals(DEFAULT_PENDING_EVALUATION.maxGrade, content.maxGrade)
			assertTrue(content.attemptItems.any { item -> item.attempt == DEFAULT_EVALUATION_SUBJECT && item.isSelected })
			assertTrue(content.typeItems.any(EvaluationTypePickerItem::isSelected))

			awaitComplete()
		}

		assertTrue(effects.isEmpty())
	}

	@Test
	fun loadEvaluationActionProcessor_selectsSubjectById_whenSubjectCodeDiffers() = runTest {
		val availableSubject = DEFAULT_EVALUATION_SUBJECT.copy(code = "INF-101-A")
		val processor = LoadEvaluationActionProcessor(
			getEvaluationAndAvailableAttemptsUseCase = GetEvaluationAndAvailableAttemptsUseCase(
				RecordingEvaluationRepository(
					initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
					availableSubjects = listOf(availableSubject, SECOND_EVALUATION_SUBJECT)
				),
				reportingRepository = RecordingReportingRepository()
			)
		)

		processor.process(
			action = Evaluation.Action.LoadEvaluation(DEFAULT_PENDING_EVALUATION.id),
			sideEffect = {}
		).test {
			assertEquals(Evaluation.State.Loading, awaitItem()(Evaluation.State.Failed))

			val content = assertIs<Evaluation.State.Content>(awaitItem()(Evaluation.State.Loading))
			assertEquals(availableSubject, content.selectedAttempt)
			assertTrue(content.attemptItems.any { item -> item.attempt == availableSubject && item.isSelected })

			awaitComplete()
		}
	}
}
