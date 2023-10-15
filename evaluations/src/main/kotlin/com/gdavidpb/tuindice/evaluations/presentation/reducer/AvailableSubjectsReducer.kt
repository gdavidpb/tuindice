package com.gdavidpb.tuindice.evaluations.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AvailableSubjectsReducer
	: BaseReducer<AddEvaluation.State, AddEvaluation.Event, List<Subject>, Nothing>() {
	override suspend fun reduceLoadingState(
		currentState: AddEvaluation.State,
		useCaseState: UseCaseState.Loading<List<Subject>, Nothing>
	): Flow<ViewOutput> {
		return flowOf(AddEvaluation.State.Loading)
	}

	override suspend fun reduceDataState(
		currentState: AddEvaluation.State,
		useCaseState: UseCaseState.Data<List<Subject>, Nothing>
	): Flow<ViewOutput> {
		return flowOf(
			AddEvaluation.State.Content(
				availableSubjects = useCaseState.value
			)
		)
	}

	override suspend fun reduceErrorState(
		currentState: AddEvaluation.State,
		useCaseState: UseCaseState.Error<List<Subject>, Nothing>
	): Flow<ViewOutput> {
		return flowOf(AddEvaluation.State.Failed)
	}
}