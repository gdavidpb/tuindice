package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.containsQuarterInViewMode
import com.gdavidpb.tuindice.record.domain.model.filterByViewMode
import com.gdavidpb.tuindice.record.domain.model.other
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedQuarterIdUseCase
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSelectedQuarterIdParams
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last

class SelectQuarterActionProcessor(
	private val setSelectedQuarterIdUseCase: SetSelectedQuarterIdUseCase
) : ActionProcessor<Record.State, Record.Action.SelectQuarter, Record.Effect>() {

	override suspend fun process(
		action: Record.Action.SelectQuarter,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return flowOf(
			suspend { state: Record.State ->
				when (state) {
					is Record.State.Content ->
						if (state.quarters.filterByViewMode(state.viewMode).any { quarter ->
								quarter.id == action.quarterId
							}
						) {
							persistSelectedQuarterId(
								viewMode = state.viewMode,
								quarterId = action.quarterId
							)

							if (
								state.quarters.containsQuarterInViewMode(
									viewMode = state.viewMode.other(),
									quarterId = action.quarterId
								)
							) {
								persistSelectedQuarterId(
									viewMode = state.viewMode.other(),
									quarterId = action.quarterId
								)
							}

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
		)
	}

	private suspend fun persistSelectedQuarterId(viewMode: RecordViewMode, quarterId: String) {
		setSelectedQuarterIdUseCase.execute(
			SetSelectedQuarterIdParams(
				viewMode = viewMode,
				quarterId = quarterId
			)
		).last()
	}
}
