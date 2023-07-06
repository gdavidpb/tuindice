package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class AvailableSubjectsReducer
	: BaseReducer<Evaluation.State, Evaluation.Event, List<Subject>, Nothing>() {

	override fun reduceUnrecoverableState(
		currentState: Evaluation.State,
		throwable: Throwable
	): Flow<ViewOutput> {
		return flowOf(Evaluation.State.Failed)
	}

	override suspend fun reduceLoadingState(
		currentState: Evaluation.State,
		useCaseState: UseCaseState.Loading<List<Subject>, Nothing>
	): Flow<ViewOutput> {
		return flowOf(Evaluation.State.Loading)
	}

	override suspend fun reduceDataState(
		currentState: Evaluation.State,
		useCaseState: UseCaseState.Data<List<Subject>, Nothing>
	): Flow<ViewOutput> {
		return flow {
			val subjects = useCaseState.value

			if (currentState is Evaluation.State.Content)
				emit(
					currentState.copy(
						availableSubjects = subjects
					)
				)
			else
				emit(
					Evaluation.State.Content(
						availableSubjects = subjects
					)
				)
		}
	}

	override suspend fun reduceErrorState(
		currentState: Evaluation.State,
		useCaseState: UseCaseState.Error<List<Subject>, Nothing>
	): Flow<ViewOutput> {
		return flowOf(Evaluation.State.Failed)
	}
}