package com.gdavidpb.tuindice.record.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersError
import com.gdavidpb.tuindice.record.presentation.contract.Record
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
		val quarters = useCaseState.value

		return flow {
			if (quarters.isNotEmpty())
				emit(
					Record.State.Content(quarters)
				)
			else
				emit(
					Record.State.Empty
				)
		}
	}

	override suspend fun reduceErrorState(
		currentState: Record.State,
		useCaseState: UseCaseState.Error<List<Quarter>, GetQuartersError>
	): Flow<ViewOutput> {
		return flow {
			when (val error = useCaseState.error) {
				is GetQuartersError.NoConnection ->
					emit(
						Record.Event.ShowSnackBar(
							message = if (error.isNetworkAvailable)
								resourceResolver.getString(R.string.snack_service_unavailable)
							else
								resourceResolver.getString(R.string.snack_network_unavailable)
						)
					)

				is GetQuartersError.OutdatedPassword ->
					emit(
						Record.Event.NavigateToOutdatedPassword
					)

				is GetQuartersError.Timeout ->
					emit(
						Record.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_timeout)
						)
					)

				is GetQuartersError.Unavailable ->
					emit(
						Record.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_service_unavailable)
						)
					)

				else ->
					emit(
						Record.Event.ShowSnackBar(
							message = resourceResolver.getString(R.string.snack_default_error)
						)
					)
			}

			emit(Record.State.Failed)
		}
	}
}