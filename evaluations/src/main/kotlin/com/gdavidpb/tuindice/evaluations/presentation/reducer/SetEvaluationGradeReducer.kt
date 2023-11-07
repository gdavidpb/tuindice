package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SetEvaluationGradeReducer(
	private val resourceResolver: ResourceResolver
) : BaseReducer<Evaluations.State, Evaluations.Event, Unit, UpdateEvaluationError>() {

	override suspend fun reduceDataState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Data<Unit, UpdateEvaluationError>
	): Flow<ViewOutput> {
		return flow {
			emit(
				Evaluations.Event.ShowSnackBar(
					message = resourceResolver.getString(R.string.snack_evaluation_set_grade)
				)
			)
		}
	}

	override suspend fun reduceErrorState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Error<Unit, UpdateEvaluationError>
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