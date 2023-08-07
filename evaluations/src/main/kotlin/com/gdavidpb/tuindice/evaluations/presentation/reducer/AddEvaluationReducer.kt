package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow

class AddEvaluationReducer :
	BaseReducer<Evaluation.State, Evaluation.Event, Unit, EvaluationError>() {

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
		return super.reduceErrorState(currentState, useCaseState)
	}
}