package com.gdavidpb.tuindice.record.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectError
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateSubjectReducer : BaseReducer<Record.State, Record.Event, Unit, SubjectError>() {

	override fun reduceUnrecoverableState(
		currentState: Record.State,
		throwable: Throwable
	): Flow<ViewOutput> {
		return flowOf(Record.Event.ShowDefaultErrorSnackBar)
	}

	override suspend fun reduceErrorState(
		currentState: Record.State,
		useCaseState: UseCaseState.Error<Unit, SubjectError>
	): Flow<ViewOutput> {
		return flowOf(Record.Event.ShowDefaultErrorSnackBar)
	}
}