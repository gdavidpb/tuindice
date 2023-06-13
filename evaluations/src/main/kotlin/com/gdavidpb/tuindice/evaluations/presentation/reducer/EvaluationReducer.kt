package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class EvaluationReducer : BaseReducer<Evaluations.State, Evaluations.Event, Evaluation, Nothing>() {

	override fun reduceUnrecoverableState(
		currentState: Evaluations.State,
		throwable: Throwable
	): Flow<ViewOutput> {
		return flowOf(Evaluations.State.Failed)
	}

	override suspend fun reduceLoadingState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Loading<Evaluation, Nothing>
	): Flow<ViewOutput> {
		return flowOf(Evaluations.State.Loading)
	}

	override suspend fun reduceDataState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Data<Evaluation, Nothing>
	): Flow<ViewOutput> {
		return super.reduceDataState(currentState, useCaseState)
	}

	override suspend fun reduceErrorState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Error<Evaluation, Nothing>
	): Flow<ViewOutput> {
		return flowOf(Evaluations.State.Failed)
	}
}