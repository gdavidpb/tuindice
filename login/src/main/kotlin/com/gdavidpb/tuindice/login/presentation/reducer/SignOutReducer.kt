package com.gdavidpb.tuindice.login.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.login.presentation.contract.SignOut
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SignOutReducer : BaseReducer<SignOut.State, SignOut.Event, Unit, Nothing>() {
	override suspend fun reduceLoadingState(
		currentState: SignOut.State,
		useCaseState: UseCaseState.Loading<Unit, Nothing>
	): Flow<ViewOutput> {
		return flowOf(SignOut.State.LoggingOut)
	}

	override suspend fun reduceDataState(
		currentState: SignOut.State,
		useCaseState: UseCaseState.Data<Unit, Nothing>
	): Flow<ViewOutput> {
		return flowOf(SignOut.Event.NavigateToSignIn)
	}
}