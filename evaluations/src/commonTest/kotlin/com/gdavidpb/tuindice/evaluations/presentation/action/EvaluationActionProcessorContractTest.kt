package com.gdavidpb.tuindice.evaluations.presentation.action

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.AddEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.action.evaluation.EditEvaluationActionProcessor
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.FakeIdentifierRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.evaluationContentState
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.snack_evaluation_already_exists
import tuindice.evaluations.generated.resources.snack_evaluation_not_found
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EvaluationActionProcessorContractTest {
	@Test
	fun addEvaluationActionProcessor_keepsFormAndShowsAlreadyExistsMessage() = runTest {
		val processor = AddEvaluationActionProcessor(
			addEvaluationUseCase = AddEvaluationUseCase(
				evaluationRepository = RecordingEvaluationRepository(
					addThrowable = clientRequestException(
						HttpStatusCode.PreconditionFailed,
						path = "/evaluations/v1"
					)
				),
				identifierRepository = FakeIdentifierRepository(),
				paramsValidator = AddEvaluationParamsValidator(),
				exceptionHandler = AddEvaluationExceptionHandler(
					reportingRepository = RecordingReportingRepository()
				)
			)
		)
		val initialState = evaluationContentState()
		val effects = mutableListOf<Evaluation.Effect>()

		processor.process(
			action = Evaluation.Action.ClickAddEvaluation(
				subject = DEFAULT_EVALUATION_SUBJECT,
				type = EvaluationType.QUIZ,
				scheduleMode = EvaluationScheduleMode.DATED,
				date = initialState.date,
				grade = initialState.grade,
				maxGrade = initialState.maxGrade
			),
			sideEffect = effects::add
		).test {
			assertEquals(initialState, awaitItem()(initialState))
			assertEquals(initialState, awaitItem()(initialState))
			awaitComplete()
		}

		val effect = assertIs<Evaluation.Effect.ShowSnackBar>(effects.single())
		assertEquals(getString(Res.string.snack_evaluation_already_exists), effect.message)
	}

	@Test
	fun editEvaluationActionProcessor_keepsForm_showsNotFoundMessage_andNavigatesBack() = runTest {
		val processor = EditEvaluationActionProcessor(
			updateEvaluationUseCase = UpdateEvaluationUseCase(
				evaluationRepository = RecordingEvaluationRepository(
					initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
					updateThrowable = clientRequestException(
						HttpStatusCode.NotFound,
						path = "/evaluations/v1/${DEFAULT_PENDING_EVALUATION.id}"
					)
				),
				exceptionHandler = UpdateEvaluationExceptionHandler(
					reportingRepository = RecordingReportingRepository()
				)
			)
		)
		val initialState = evaluationContentState()
		val effects = mutableListOf<Evaluation.Effect>()

		processor.process(
			action = Evaluation.Action.ClickEditEvaluation(
				evaluationId = DEFAULT_PENDING_EVALUATION.id,
				subject = DEFAULT_EVALUATION_SUBJECT,
				type = EvaluationType.QUIZ,
				scheduleMode = EvaluationScheduleMode.DATED,
				date = initialState.date,
				grade = 18.0,
				maxGrade = initialState.maxGrade
			),
			sideEffect = effects::add
		).test {
			assertEquals(initialState, awaitItem()(initialState))
			assertEquals(initialState, awaitItem()(initialState))
			awaitComplete()
		}

		val snackBar = assertIs<Evaluation.Effect.ShowSnackBar>(effects.first())
		assertEquals(getString(Res.string.snack_evaluation_not_found), snackBar.message)
		assertIs<Evaluation.Effect.NavigateToEvaluations>(effects.last())
	}
}
