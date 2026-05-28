package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSelectedTermParams
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SelectRecordTermActionProcessor(
	private val setSelectedTermUseCase: SetSelectedTermUseCase
) : ActionProcessor<Record.State, Record.Action.SelectTerm, Record.Effect>() {
	override suspend fun process(
		action: Record.Action.SelectTerm,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return setSelectedTermUseCase.execute(
			SetSelectedTermParams(
				viewMode = action.viewMode,
				termId = action.termId
			)
		).map {
			suspend { state: Record.State ->
				state
			}
		}
	}
}
