package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AddEvaluationReducer(
	private val resourceResolver: ResourceResolver
) : BaseReducer<Evaluation.State, Evaluation.Event, Unit, EvaluationError>() {

	override suspend fun reduceDataState(
		currentState: Evaluation.State,
		useCaseState: UseCaseState.Data<Unit, EvaluationError>
	): Flow<ViewOutput> {
		return super.reduceDataState(currentState, useCaseState)
	}

	override suspend fun reduceErrorState(
		currentState: Evaluation.State,
		useCaseState: UseCaseState.Error<Unit, EvaluationError>
	): Flow<ViewOutput> {
		return if (currentState is Evaluation.State.Content)
			flow {
				val error = useCaseState.error

				emit(
					currentState.copy(error = error)
				)

				when (error) {
					is EvaluationError.EmptyName ->
						emit(
							Evaluation.Event.ShowSnackBar(
								message = resourceResolver.getString(R.string.error_evaluation_name_empty)
							)
						)

					is EvaluationError.SubjectMissed ->
						emit(
							Evaluation.Event.ShowSnackBar(
								message = resourceResolver.getString(R.string.error_evaluation_subject_missed)
							)
						)

					is EvaluationError.GradeMissed ->
						emit(
							Evaluation.Event.ShowSnackBar(
								message = resourceResolver.getString(R.string.error_evaluation_grade_missed)
							)
						)

					is EvaluationError.InvalidGradeStep ->
						emit(
							Evaluation.Event.ShowSnackBar(
								message = resourceResolver.getString(R.string.error_evaluation_grade_invalid_step)
							)
						)

					is EvaluationError.OutOfRangeGrade ->
						emit(
							Evaluation.Event.ShowSnackBar(
								message = resourceResolver.getString(R.string.error_evaluation_grade_invalid_range)
							)
						)

					is EvaluationError.MaxGradeMissed ->
						emit(
							Evaluation.Event.ShowSnackBar(
								message = resourceResolver.getString(R.string.error_evaluation_max_grade_missed)
							)
						)

					is EvaluationError.TypeMissed ->
						emit(
							Evaluation.Event.ShowSnackBar(
								message = resourceResolver.getString(R.string.error_evaluation_type_missed)
							)
						)

					else ->
						emit(
							Evaluation.Event.ShowSnackBar(
								message = resourceResolver.getString(R.string.snack_default_error)
							)
						)
				}
			}
		else
			super.reduceErrorState(currentState, useCaseState)
	}
}