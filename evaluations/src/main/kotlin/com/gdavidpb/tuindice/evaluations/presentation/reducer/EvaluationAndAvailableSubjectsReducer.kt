package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAndAvailableSubjects
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class EvaluationAndAvailableSubjectsReducer :
	BaseReducer<Evaluation.State, Evaluation.Event, EvaluationAndAvailableSubjects, Nothing>() {

	override fun reduceUnrecoverableState(
		currentState: Evaluation.State,
		throwable: Throwable
	): Flow<ViewOutput> {
		return flowOf(Evaluation.State.Failed)
	}

	override suspend fun reduceLoadingState(
		currentState: Evaluation.State,
		useCaseState: UseCaseState.Loading<EvaluationAndAvailableSubjects, Nothing>
	): Flow<ViewOutput> {
		return flowOf(Evaluation.State.Loading)
	}

	override suspend fun reduceDataState(
		currentState: Evaluation.State,
		useCaseState: UseCaseState.Data<EvaluationAndAvailableSubjects, Nothing>
	): Flow<ViewOutput> {
		return flowOf(
			with(useCaseState.value) {
				Evaluation.State.Content(
					availableSubjects = availableSubjects,
					subject = evaluation?.subject,
					name = evaluation?.name,
					grade = evaluation?.grade,
					maxGrade = evaluation?.maxGrade
				)
			}
		)
	}

	override suspend fun reduceErrorState(
		currentState: Evaluation.State,
		useCaseState: UseCaseState.Error<EvaluationAndAvailableSubjects, Nothing>
	): Flow<ViewOutput> {
		return flowOf(Evaluation.State.Failed)
	}
}