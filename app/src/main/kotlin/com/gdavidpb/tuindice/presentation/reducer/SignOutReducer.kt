package com.gdavidpb.tuindice.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignOutReducer : BaseReducer<Main.State, Main.Event, Unit, Nothing>() {
	override suspend fun reduceDataState(
		currentState: Main.State,
		useCaseState: UseCaseState.Data<Unit, Nothing>
	): Flow<ViewOutput> {
		return flowOf(Main.Event.NavigateToSignIn)
	}
}