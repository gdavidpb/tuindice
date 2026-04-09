package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SetRecordViewModeActionProcessor(
	private val setRecordViewModeUseCase: SetRecordViewModeUseCase
) : ActionProcessor<Record.State, Record.Action.SetViewMode, Record.Effect>() {
	override suspend fun process(
		action: Record.Action.SetViewMode,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return setRecordViewModeUseCase.execute(action.viewMode)
			.map {
				suspend { state: Record.State ->
					state
				}
			}
	}
}
