package com.gdavidpb.tuindice.evaluations.presentation.action

import app.cash.turbine.test
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationAndAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.LoadEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluations.LoadEvaluationsActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationTypePickerItem
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
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
	fun loadEvaluationsActionProcessor_reducesStateToContent_withAvailableFilters() = runTest {
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
				reportingRepository = RecordingReportingRepository()
			)
		)
		val effects = mutableListOf<Evaluations.Effect>()

		processor.process(
			action = Evaluations.Action.LoadEvaluations(activeFilters = flowOf(emptyList())),
			sideEffect = effects::add
		).test {
			assertEquals(Evaluations.State.Loading, awaitItem()(Evaluations.State.Empty))

			val content = assertIs<Evaluations.State.Content>(awaitItem()(Evaluations.State.Loading))
			assertEquals(2, content.evaluationGroups.flatMap { group -> group.items }.size)
			assertTrue(
				content.filterGroups
					.flatMap { group -> group.items }
					.any { item -> item.filter.getLabel() == DEFAULT_EVALUATION_SUBJECT.code }
			)

			awaitComplete()
		}

		assertTrue(effects.isEmpty())
	}

	@Test
	fun loadEvaluationsActionProcessor_keepsLoading_whenInitialSnapshotIsEmpty() = runTest {
		val processor = LoadEvaluationsActionProcessor(
			getEvaluationsUseCase = GetEvaluationsUseCase(
				evaluationRepository = RecordingEvaluationRepository(
					evaluationsFlow = flowOf(emptyList()),
					availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
				),
				reportingRepository = RecordingReportingRepository()
			)
		)

		processor.process(
			action = Evaluations.Action.LoadEvaluations(activeFilters = flowOf(emptyList())),
			sideEffect = {}
		).test {
			assertEquals(Evaluations.State.Loading, awaitItem()(Evaluations.State.Empty))
			assertEquals(Evaluations.State.Loading, awaitItem()(Evaluations.State.Loading))

			awaitComplete()
		}
	}

	@Test
	fun loadEvaluationsActionProcessor_reducesStateToNoSubjects_withoutReportingError() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val processor = LoadEvaluationsActionProcessor(
			getEvaluationsUseCase = GetEvaluationsUseCase(
				evaluationRepository = RecordingEvaluationRepository(
					evaluationsFlow = flowOf(emptyList()),
					availableSubjects = emptyList()
				),
				reportingRepository = reportingRepository
			)
		)

		processor.process(
			action = Evaluations.Action.LoadEvaluations(activeFilters = flowOf(emptyList())),
			sideEffect = {}
		).test {
			assertEquals(Evaluations.State.Loading, awaitItem()(Evaluations.State.Empty))
			assertEquals(Evaluations.State.NoSubjects, awaitItem()(Evaluations.State.Loading))

			awaitComplete()
		}

		assertTrue(reportingRepository.exceptions.isEmpty())
	}

	@Test
	fun loadEvaluationActionProcessor_reducesStateToEditableContent() = runTest {
		val processor = LoadEvaluationActionProcessor(
			getEvaluationAndAvailableSubjectsUseCase = GetEvaluationAndAvailableSubjectsUseCase(
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
			assertEquals(DEFAULT_EVALUATION_SUBJECT, content.selectedSubject)
			assertEquals(DEFAULT_PENDING_EVALUATION.maxGrade, content.maxGrade)
			assertTrue(content.subjectItems.any { item -> item.subject == DEFAULT_EVALUATION_SUBJECT && item.isSelected })
			assertTrue(content.typeItems.any(EvaluationTypePickerItem::isSelected))

			awaitComplete()
		}

		assertTrue(effects.isEmpty())
	}

	@Test
	fun loadEvaluationActionProcessor_selectsSubjectById_whenSubjectCodeDiffers() = runTest {
		val availableSubject = DEFAULT_EVALUATION_SUBJECT.copy(code = "INF-101-A")
		val processor = LoadEvaluationActionProcessor(
			getEvaluationAndAvailableSubjectsUseCase = GetEvaluationAndAvailableSubjectsUseCase(
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
			assertEquals(availableSubject, content.selectedSubject)
			assertTrue(content.subjectItems.any { item -> item.subject == availableSubject && item.isSelected })

			awaitComplete()
		}
	}
}
