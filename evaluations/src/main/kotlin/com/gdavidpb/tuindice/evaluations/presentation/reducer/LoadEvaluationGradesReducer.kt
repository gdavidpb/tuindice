package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoadEvaluationGradesReducer(
	private val resourceResolver: ResourceResolver
) : BaseReducer<Evaluations.State, Evaluations.Event, Evaluation, EvaluationsError>() {

	override suspend fun reduceDataState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Data<Evaluation, EvaluationsError>
	): Flow<ViewOutput> {
		val evaluation = useCaseState.value

		return flow {
			emit(
				Evaluations.Event.ShowGradePickerDialog(
					evaluationId = evaluation.evaluationId,
					grade = evaluation.grade,
					maxGrade = evaluation.maxGrade
				)
			)
		}
	}

	override suspend fun reduceErrorState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Error<Evaluation, EvaluationsError>
	): Flow<ViewOutput> {
		return flow {
			emit(
				Evaluations.Event.ShowSnackBar(
					message = resourceResolver.getString(R.string.snack_default_error)
				)
			)
		}
	}
}