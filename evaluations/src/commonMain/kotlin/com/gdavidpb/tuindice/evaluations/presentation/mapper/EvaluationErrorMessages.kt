package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.commonNetworkUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonServiceUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonTimeoutMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonUnexpectedErrorMessage
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.RemoveEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationUseCaseError
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.error_evaluation_max_grade_missed
import tuindice.evaluations.generated.resources.error_evaluation_subject_missed
import tuindice.evaluations.generated.resources.error_evaluation_type_missed
import tuindice.evaluations.generated.resources.snack_evaluation_already_exists
import tuindice.evaluations.generated.resources.snack_evaluation_not_found

internal suspend fun EvaluationsUseCaseError?.toEvaluationsFailedMessage(): String {
	return when (this) {
		is EvaluationsUseCaseError.NoConnection ->
			if (isNetworkAvailable) {
				commonServiceUnavailableMessage()
			} else {
				commonNetworkUnavailableMessage()
			}

		EvaluationsUseCaseError.Timeout ->
			commonTimeoutMessage()

		EvaluationsUseCaseError.Unavailable ->
			commonServiceUnavailableMessage()

		else ->
			commonUnexpectedErrorMessage()
	}
}

internal suspend fun AddEvaluationUseCaseError?.toAddSubmitErrorMessage(): String {
	return when (this) {
		is AddEvaluationUseCaseError.AlreadyExists ->
			getString(Res.string.snack_evaluation_already_exists)

		is AddEvaluationUseCaseError.AttemptMissed ->
			getString(Res.string.error_evaluation_subject_missed)

		is AddEvaluationUseCaseError.TypeMissed ->
			getString(Res.string.error_evaluation_type_missed)

		is AddEvaluationUseCaseError.MaxGradeMissed ->
			getString(Res.string.error_evaluation_max_grade_missed)

		else ->
			commonUnexpectedErrorMessage()
	}
}

internal suspend fun UpdateEvaluationUseCaseError?.toEditSubmitErrorMessage(): String {
	return when (this) {
		is UpdateEvaluationUseCaseError.NotFound ->
			getString(Res.string.snack_evaluation_not_found)

		else ->
			commonUnexpectedErrorMessage()
	}
}

internal suspend fun UpdateEvaluationUseCaseError?.toGradeSaveErrorMessage(): String {
	return toEditSubmitErrorMessage()
}

internal suspend fun RemoveEvaluationUseCaseError?.toRemoveErrorMessage(): String {
	return when (this) {
		is RemoveEvaluationUseCaseError.NotFound ->
			getString(Res.string.snack_evaluation_not_found)

		else ->
			commonUnexpectedErrorMessage()
	}
}
