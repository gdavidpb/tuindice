package com.gdavidpb.tuindice.record.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.param.RemoveQuarterParams
import com.gdavidpb.tuindice.record.presentation.contract.Record

class RemoveQuarterReducer(
	override val useCase: RemoveQuarterUseCase
) : BaseReducer<RemoveQuarterParams, Unit, Nothing, Record.State, Record.Action.RemoveQuarter, Record.Event>() {
	override fun actionToParams(action: Record.Action.RemoveQuarter): RemoveQuarterParams {
		return action.params
	}

	override fun reduceUnrecoverableState(
		currentState: Record.State,
		throwable: Throwable,
		eventProducer: (Record.Event) -> Unit
	): Record.State? {
		eventProducer(Record.Event.ShowDefaultErrorSnackBar)

		return super.reduceUnrecoverableState(currentState, throwable, eventProducer)
	}

	override suspend fun reduceErrorState(
		currentState: Record.State,
		useCaseState: UseCaseState.Error<Unit, Nothing>,
		eventProducer: (Record.Event) -> Unit
	): Record.State? {
		eventProducer(Record.Event.ShowDefaultErrorSnackBar)

		return reduceErrorState(currentState, useCaseState, eventProducer)
	}
}