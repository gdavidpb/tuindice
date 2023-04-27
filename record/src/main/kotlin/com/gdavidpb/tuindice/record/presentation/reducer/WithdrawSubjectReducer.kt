package com.gdavidpb.tuindice.record.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.record.domain.usecase.WithdrawSubjectUseCase
import com.gdavidpb.tuindice.record.domain.usecase.param.WithdrawSubjectParams
import com.gdavidpb.tuindice.record.presentation.contract.Record

class WithdrawSubjectReducer(
	override val useCase: WithdrawSubjectUseCase
) : BaseReducer<WithdrawSubjectParams, Unit, Nothing, Record.State, Record.Action.WithdrawSubject, Record.Event>() {
	override fun actionToParams(action: Record.Action.WithdrawSubject): WithdrawSubjectParams {
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

		return super.reduceErrorState(currentState, useCaseState, eventProducer)
	}
}