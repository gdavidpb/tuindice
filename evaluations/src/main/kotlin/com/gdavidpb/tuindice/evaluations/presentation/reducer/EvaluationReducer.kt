package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import com.gdavidpb.tuindice.base.domain.model.Evaluation as EvaluationDomain

class EvaluationReducer :
	BaseReducer<Evaluation.State, Evaluation.Event, EvaluationDomain, Nothing>() {

	override fun reduceUnrecoverableState(
		currentState: Evaluation.State,
		throwable: Throwable
	): Flow<ViewOutput> {
		return flowOf(Evaluation.State.Failed)
	}

	override suspend fun reduceLoadingState(
		currentState: Evaluation.State,
		useCaseState: UseCaseState.Loading<EvaluationDomain, Nothing>
	): Flow<ViewOutput> {
		return flowOf(Evaluation.State.Loading)
	}

	override suspend fun reduceDataState(
		currentState: Evaluation.State,
		useCaseState: UseCaseState.Data<EvaluationDomain, Nothing>
	): Flow<ViewOutput> {
		return flow {
			val evaluation = useCaseState.value

			emit(
				Evaluation.State.Content(
					availableSubjects = listOf(),
					subject = null,
					name = evaluation.name,
					grade = evaluation.grade,
					maxGrade = evaluation.maxGrade
				)
			)
		}
	}

	override suspend fun reduceErrorState(
		currentState: Evaluation.State,
		useCaseState: UseCaseState.Error<EvaluationDomain, Nothing>
	): Flow<ViewOutput> {
		return flowOf(Evaluation.State.Failed)
	}
}