package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.commonUnexpectedErrorMessage
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.RemoveEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationUseCaseError
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.error_evaluation_max_grade_missed
import tuindice.evaluations.generated.resources.error_evaluation_subject_missed
import tuindice.evaluations.generated.resources.error_evaluation_type_missed
import tuindice.evaluations.generated.resources.snack_evaluation_already_exists
import tuindice.evaluations.generated.resources.snack_evaluation_not_found
import kotlin.test.Test
import kotlin.test.assertEquals

// Resolves messages through getString, so it runs on the iOS host only (androidHostTestExcludedPatterns).
class EvaluationErrorMessagesMappingUiTest {
	@Test
	fun toAddSubmitErrorMessage_whenErrorIsKnown_mapsToItsResource() = runTest {
		assertEquals(
			getString(Res.string.snack_evaluation_already_exists),
			AddEvaluationUseCaseError.AlreadyExists.toAddSubmitErrorMessage()
		)
		assertEquals(
			getString(Res.string.error_evaluation_subject_missed),
			AddEvaluationUseCaseError.AttemptMissed.toAddSubmitErrorMessage()
		)
		assertEquals(
			getString(Res.string.error_evaluation_type_missed),
			AddEvaluationUseCaseError.TypeMissed.toAddSubmitErrorMessage()
		)
		assertEquals(
			getString(Res.string.error_evaluation_max_grade_missed),
			AddEvaluationUseCaseError.MaxGradeMissed.toAddSubmitErrorMessage()
		)
	}

	@Test
	fun toAddSubmitErrorMessage_whenErrorIsUnknown_fallsBackToDefaultMessage() = runTest {
		assertEquals(
			commonUnexpectedErrorMessage(),
			(null as AddEvaluationUseCaseError?).toAddSubmitErrorMessage()
		)
	}

	@Test
	fun toEditSubmitErrorMessage_whenEvaluationIsNotFound_mapsToNotFoundResource() = runTest {
		assertEquals(
			getString(Res.string.snack_evaluation_not_found),
			UpdateEvaluationUseCaseError.NotFound.toEditSubmitErrorMessage()
		)
	}

	@Test
	fun toEditSubmitErrorMessage_whenErrorIsUnknown_fallsBackToDefaultMessage() = runTest {
		assertEquals(
			commonUnexpectedErrorMessage(),
			(null as UpdateEvaluationUseCaseError?).toEditSubmitErrorMessage()
		)
	}

	@Test
	fun toGradeSaveErrorMessage_whenInvoked_delegatesToEditMessages() = runTest {
		assertEquals(
			UpdateEvaluationUseCaseError.NotFound.toEditSubmitErrorMessage(),
			UpdateEvaluationUseCaseError.NotFound.toGradeSaveErrorMessage()
		)
		assertEquals(
			(null as UpdateEvaluationUseCaseError?).toEditSubmitErrorMessage(),
			(null as UpdateEvaluationUseCaseError?).toGradeSaveErrorMessage()
		)
	}

	@Test
	fun toRemoveErrorMessage_whenEvaluationIsNotFound_mapsToNotFoundResource() = runTest {
		assertEquals(
			getString(Res.string.snack_evaluation_not_found),
			RemoveEvaluationUseCaseError.NotFound.toRemoveErrorMessage()
		)
	}

	@Test
	fun toRemoveErrorMessage_whenErrorIsUnknown_fallsBackToDefaultMessage() = runTest {
		assertEquals(
			commonUnexpectedErrorMessage(),
			(null as RemoveEvaluationUseCaseError?).toRemoveErrorMessage()
		)
	}
}
