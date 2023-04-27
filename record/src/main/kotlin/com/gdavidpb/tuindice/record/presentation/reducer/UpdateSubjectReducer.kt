package com.gdavidpb.tuindice.record.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSubjectUseCase
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectError
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.presentation.contract.Record

class UpdateSubjectReducer(
	override val useCase: UpdateSubjectUseCase
) : BaseReducer<UpdateSubjectParams, Unit, SubjectError, Record.State, Record.Action.UpdateSubject, Record.Event>() {
	override fun actionToParams(action: Record.Action.UpdateSubject): UpdateSubjectParams {
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
		useCaseState: UseCaseState.Error<Unit, SubjectError>,
		eventProducer: (Record.Event) -> Unit
	): Record.State? {
		eventProducer(Record.Event.ShowDefaultErrorSnackBar)

		return super.reduceErrorState(currentState, useCaseState, eventProducer)
	}
}