package com.gdavidpb.tuindice.summary.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignOutReducer : BaseReducer<Summary.State, Summary.Event, Unit, Nothing>() {
	override suspend fun reduceDataState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Data<Unit, Nothing>
	): Flow<ViewOutput> {
		return flowOf(Summary.Event.NavigateToSignIn)
	}
}