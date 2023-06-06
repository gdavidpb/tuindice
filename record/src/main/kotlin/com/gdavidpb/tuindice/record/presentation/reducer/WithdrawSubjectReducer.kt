package com.gdavidpb.tuindice.record.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.base.utils.extension.ViewOutput
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class WithdrawSubjectReducer(
	private val resourceResolver: ResourceResolver
) : BaseReducer<Record.State, Record.Event, Unit, Nothing>() {

	override fun reduceUnrecoverableState(
		currentState: Record.State,
		throwable: Throwable
	): Flow<ViewOutput> {
		return flowOf(
			Record.Event.ShowSnackBar(
				message = resourceResolver.getString(R.string.snack_default_error)
			)
		)
	}

	override suspend fun reduceErrorState(
		currentState: Record.State,
		useCaseState: UseCaseState.Error<Unit, Nothing>
	): Flow<ViewOutput> {
		return flowOf(
			Record.Event.ShowSnackBar(
				message = resourceResolver.getString(R.string.snack_default_error)
			)
		)
	}
}