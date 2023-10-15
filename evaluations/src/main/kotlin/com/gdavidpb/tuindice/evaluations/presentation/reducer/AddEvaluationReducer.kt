package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationError
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AddEvaluationReducer(
	private val resourceResolver: ResourceResolver
) :
	BaseReducer<AddEvaluation.State, AddEvaluation.Event, Unit, AddEvaluationError>() {
	override suspend fun reduceErrorState(
		currentState: AddEvaluation.State,
		useCaseState: UseCaseState.Error<Unit, AddEvaluationError>
	): Flow<ViewOutput> {
		return flow {
			when (useCaseState.error) {
				is AddEvaluationError.SubjectMissed ->
					emit(
						AddEvaluation.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.error_evaluation_subject_missed)
						)
					)

				is AddEvaluationError.TypeMissed ->
					emit(
						AddEvaluation.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.error_evaluation_type_missed)
						)
					)

				is AddEvaluationError.MaxGradeMissed ->
					emit(
						AddEvaluation.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.error_evaluation_max_grade_missed)
						)
					)

				else ->
					emit(
						AddEvaluation.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_default_error)
						)
					)
			}
		}
	}
}