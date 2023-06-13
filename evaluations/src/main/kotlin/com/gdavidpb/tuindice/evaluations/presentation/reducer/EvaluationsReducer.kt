package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class EvaluationsReducer :
	BaseReducer<Evaluations.State, Evaluations.Event, Map<String, List<Evaluation>>, EvaluationsError>() {

	override fun reduceUnrecoverableState(
		currentState: Evaluations.State,
		throwable: Throwable
	): Flow<ViewOutput> {
		return flowOf(Evaluations.State.Failed)
	}

	override suspend fun reduceLoadingState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Loading<Map<String, List<Evaluation>>, EvaluationsError>
	): Flow<ViewOutput> {
		return flowOf(Evaluations.State.Loading)
	}

	override suspend fun reduceDataState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Data<Map<String, List<Evaluation>>, EvaluationsError>
	): Flow<ViewOutput> {
		val evaluations = useCaseState.value

		return flow {
			if (evaluations.isNotEmpty())
				emit(
					Evaluations.State.Content(evaluations)
				)
			else
				emit(
					Evaluations.State.Empty
				)
		}
	}

	override suspend fun reduceErrorState(
		currentState: Evaluations.State,
		useCaseState: UseCaseState.Error<Map<String, List<Evaluation>>, EvaluationsError>
	): Flow<ViewOutput> {
		return flowOf(Evaluations.State.Failed)
	}
}