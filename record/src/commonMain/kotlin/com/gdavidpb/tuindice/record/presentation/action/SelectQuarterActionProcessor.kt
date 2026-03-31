package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedQuarterIdUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SelectQuarterActionProcessor(
	private val setSelectedQuarterIdUseCase: SetSelectedQuarterIdUseCase
) : ActionProcessor<Record.State, Record.Action.SelectQuarter, Record.Effect>() {

	override suspend fun process(
		action: Record.Action.SelectQuarter,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return setSelectedQuarterIdUseCase.execute(action.quarterId)
			.map {
				suspend { state: Record.State ->
					when (state) {
						is Record.State.Content ->
							if (state.quarters.any { quarter -> quarter.id == action.quarterId }) {
								state.copy(selectedQuarterId = action.quarterId)
							} else {
								state
							}

						Record.State.Empty,
						Record.State.Failed,
						Record.State.Loading,
						-> state
					}
				}
			}
	}
}
