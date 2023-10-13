package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toStep2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DoneAddEvaluationStep1Reducer(
	private val resourceResolver: ResourceResolver
) : BaseReducer<AddEvaluation.State, AddEvaluation.Event, Unit, EvaluationError>() {
	override suspend fun reduceErrorState(
		currentState: AddEvaluation.State,
		useCaseState: UseCaseState.Error<Unit, EvaluationError>
	): Flow<ViewOutput> {
		return flow {
			when (useCaseState.error) {
				is EvaluationError.SubjectMissed ->
					emit(
						AddEvaluation.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.error_evaluation_subject_missed)
						)
					)

				is EvaluationError.TypeMissed ->
					emit(
						AddEvaluation.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.error_evaluation_type_missed)
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

	override suspend fun reduceDataState(
		currentState: AddEvaluation.State,
		useCaseState: UseCaseState.Data<Unit, EvaluationError>
	): Flow<ViewOutput> {
		return flow {
			if (currentState is AddEvaluation.State.Step1) {
				emit(
					currentState.toStep2()
				)

				emit(
					AddEvaluation.Event.NavigateTo(
						subRoute = Destination.AddEvaluation.Step2
					)
				)
			}
		}
	}
}