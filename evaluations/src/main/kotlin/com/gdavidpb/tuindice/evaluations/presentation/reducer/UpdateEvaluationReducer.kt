package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateEvaluationReducer :
	BaseReducer<Evaluations.State, Evaluations.Event, Evaluation, EvaluationError>() {

	override fun reduceUnrecoverableState(
		currentState: Evaluations.State,
		throwable: Throwable
	): Flow<ViewOutput> {
		return flowOf(Evaluations.Event.ShowDefaultErrorSnackBar)
	}

	override suspend fun reduceDataState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Data<Evaluation, EvaluationError>
	): Flow<ViewOutput> {
		return flowOf(Evaluations.Event.NavigateUp)
	}

	override suspend fun reduceErrorState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Error<Evaluation, EvaluationError>
	): Flow<ViewOutput> {
		return flowOf(Evaluations.Event.ShowDefaultErrorSnackBar)
	}
}