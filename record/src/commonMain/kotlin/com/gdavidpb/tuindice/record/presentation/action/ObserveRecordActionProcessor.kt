package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class ObserveRecordActionProcessor(
	private val observeRecordUseCase: ObserveRecordUseCase
) : ActionProcessor<Record.State, Record.Action.ObserveRecord, Record.Effect>() {
	override suspend fun process(
		action: Record.Action.ObserveRecord,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return observeRecordUseCase.execute(Unit)
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> null

					is UseCaseState.Data ->
						suspend { _: Record.State ->
							val record = useCaseState.value

							if (record.selectedTermId == null) {
								Record.State.Empty
							} else {
								Record.State.Content(
									viewMode = record.viewMode,
									record = record.record,
									selectedTermId = record.selectedTermId
								)
							}
						}

					is UseCaseState.Error ->
						suspend { state: Record.State ->
							when (state) {
								is Record.State.Content -> state
								Record.State.Empty -> state
								Record.State.Failed,
								Record.State.Idle,
								Record.State.Loading,
								-> Record.State.Failed
							}
						}
				}
			}
	}
}
