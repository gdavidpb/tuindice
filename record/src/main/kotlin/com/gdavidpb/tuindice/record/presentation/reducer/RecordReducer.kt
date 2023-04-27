package com.gdavidpb.tuindice.record.presentation.reducer

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersError
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.toRecordViewState

class RecordReducer(
	override val useCase: GetQuartersUseCase,
	private val resourceResolver: ResourceResolver
) : BaseReducer<Unit, List<Quarter>, GetQuartersError, Record.State, Record.Action.LoadQuarters, Record.Event>() {

	override fun actionToParams(action: Record.Action.LoadQuarters) {}

	override fun reduceUnrecoverableState(
		currentState: Record.State,
		throwable: Throwable,
		eventProducer: (Record.Event) -> Unit
	): Record.State {
		return Record.State.Failed
	}

	override suspend fun reduceLoadingState(
		currentState: Record.State,
		useCaseState: UseCaseState.Loading<List<Quarter>, GetQuartersError>,
		eventProducer: (Record.Event) -> Unit
	): Record.State {
		return Record.State.Loading
	}

	override suspend fun reduceDataState(
		currentState: Record.State,
		useCaseState: UseCaseState.Data<List<Quarter>, GetQuartersError>,
		eventProducer: (Record.Event) -> Unit
	): Record.State {
		val recordViewState = useCaseState.value.toRecordViewState(resourceResolver)

		return Record.State.Loaded(value = recordViewState)
	}

	override suspend fun reduceErrorState(
		currentState: Record.State,
		useCaseState: UseCaseState.Error<List<Quarter>, GetQuartersError>,
		eventProducer: (Record.Event) -> Unit
	): Record.State {
		when (val error = useCaseState.error) {
			is GetQuartersError.AccountDisabled ->
				eventProducer(Record.Event.NavigateToAccountDisabled)

			is GetQuartersError.NoConnection ->
				eventProducer(Record.Event.ShowNoConnectionSnackBar(error.isNetworkAvailable))

			is GetQuartersError.OutdatedPassword ->
				eventProducer(Record.Event.NavigateToOutdatedPassword)

			is GetQuartersError.Timeout ->
				eventProducer(Record.Event.ShowTimeoutSnackBar)

			is GetQuartersError.Unavailable ->
				eventProducer(Record.Event.ShowUnavailableSnackBar)

			else ->
				eventProducer(Record.Event.ShowDefaultErrorSnackBar)
		}

		return Record.State.Failed
	}
}