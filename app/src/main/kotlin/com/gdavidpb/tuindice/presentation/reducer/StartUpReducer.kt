package com.gdavidpb.tuindice.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.domain.model.StartUpData
import com.gdavidpb.tuindice.domain.usecase.error.StartUpError
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class StartUpReducer :
	BaseReducer<Main.State, Main.Event, StartUpData, StartUpError>() {

	override fun reduceUnrecoverableState(
		currentState: Main.State,
		throwable: Throwable,
	): Flow<ViewOutput> {
		return flowOf(Main.State.Failed)
	}

	override suspend fun reduceLoadingState(
		currentState: Main.State,
		useCaseState: UseCaseState.Loading<StartUpData, StartUpError>
	): Flow<ViewOutput> {
		return flowOf(Main.State.Starting)
	}

	override suspend fun reduceDataState(
		currentState: Main.State,
		useCaseState: UseCaseState.Data<StartUpData, StartUpError>
	): Flow<ViewOutput> {
		return flowOf(
			with(useCaseState.value) {
				Main.State.Content(
					title = title,
					startDestination = startDestination,
					currentDestination = currentDestination,
					destinations = destinations,
					topBarConfig = topBarConfig
				)
			}
		)
	}

	override suspend fun reduceErrorState(
		currentState: Main.State,
		useCaseState: UseCaseState.Error<StartUpData, StartUpError>
	): Flow<ViewOutput> {
		return flow {
			when (val error = useCaseState.error) {
				is StartUpError.NoServices ->
					emit(Main.Event.ShowNoServicesDialog(status = error.status))

				else -> {}
			}

			emit(Main.State.Failed)
		}
	}
}