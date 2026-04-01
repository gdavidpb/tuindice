package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationSubjectFilter
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import com.gdavidpb.tuindice.evaluations.testing.*
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluationsUseCaseContractTest {
	@Test
	fun getEvaluationsUseCase_emitsSortedAndFilteredEvaluations() = runTest {
		val filter = EvaluationSubjectFilter(DEFAULT_EVALUATION_SUBJECT.code)
		val useCase = GetEvaluationsUseCase(
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

		useCase.execute(flowOf(listOf(filter))).test {
			val result = awaitLoadingThenData(this) as GetEvaluations.Content
			assertEquals(
				listOf(DEFAULT_COMPLETED_EVALUATION, DEFAULT_PENDING_EVALUATION),
				result.originalEvaluations
			)
			assertEquals(listOf(DEFAULT_PENDING_EVALUATION), result.filteredEvaluations)
			assertEquals(listOf(filter), result.activeFilters)
			awaitComplete()
		}
	}

	@Test
	fun getEvaluationsUseCase_returnsNoSubjectsState_whenFeatureHasNoSubjects() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(emptyList()),
				initialEvaluations = emptyList(),
				availableSubjects = emptyList()
			),
			reportingRepository = reportingRepository
		)

		useCase.execute(flowOf(emptyList())).test {
			assertEquals(GetEvaluations.NoSubjects, awaitLoadingThenData(this))
			assertTrue(reportingRepository.exceptions.isEmpty())
			awaitComplete()
		}
	}

	@Test
	fun getEvaluationAndAvailableSubjectsUseCase_emitsEvaluationAndSubjects() = runTest {
		val repository = RecordingEvaluationRepository(
			initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
		)
		val useCase = GetEvaluationAndAvailableSubjectsUseCase(
			evaluationRepository = repository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(GetEvaluationParams(DEFAULT_PENDING_EVALUATION.id)).test {
			val result = awaitLoadingThenData(this)
			assertEquals(DEFAULT_PENDING_EVALUATION, result.evaluation)
			assertEquals(
				listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
				result.availableSubjects
			)
			awaitComplete()
		}
	}
}
