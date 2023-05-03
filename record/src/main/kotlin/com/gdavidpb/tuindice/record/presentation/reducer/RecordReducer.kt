package com.gdavidpb.tuindice.record.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersError
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.toRecordViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class RecordReducer(
	private val resourceResolver: ResourceResolver
) : BaseReducer<Record.State, Record.Event, List<Quarter>, GetQuartersError>() {

	override fun reduceUnrecoverableState(
		currentState: Record.State,
		throwable: Throwable
	): Flow<ViewOutput> {
		return flowOf(Record.State.Failed)
	}

	override suspend fun reduceLoadingState(
		currentState: Record.State,
		useCaseState: UseCaseState.Loading<List<Quarter>, GetQuartersError>
	): Flow<ViewOutput> {
		return flowOf(Record.State.Loading)
	}

	override suspend fun reduceDataState(
		currentState: Record.State,
		useCaseState: UseCaseState.Data<List<Quarter>, GetQuartersError>
	): Flow<ViewOutput> {
		val recordViewState = useCaseState.value.toRecordViewState(resourceResolver)

		return flowOf(Record.State.Loaded(value = recordViewState))
	}

	override suspend fun reduceErrorState(
		currentState: Record.State,
		useCaseState: UseCaseState.Error<List<Quarter>, GetQuartersError>
	): Flow<ViewOutput> {
		return flow {
			when (val error = useCaseState.error) {
				is GetQuartersError.AccountDisabled ->
					emit(Record.Event.NavigateToAccountDisabled)

				is GetQuartersError.NoConnection ->
					emit(Record.Event.ShowNoConnectionSnackBar(error.isNetworkAvailable))

				is GetQuartersError.OutdatedPassword ->
					emit(Record.Event.NavigateToOutdatedPassword)

				is GetQuartersError.Timeout ->
					emit(Record.Event.ShowTimeoutSnackBar)

				is GetQuartersError.Unavailable ->
					emit(Record.Event.ShowUnavailableSnackBar)

				else ->
					emit(Record.Event.ShowDefaultErrorSnackBar)
			}

			emit(Record.State.Failed)
		}
	}
}