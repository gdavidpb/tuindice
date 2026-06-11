package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.UpdateRecordUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RefreshRecordActionProcessor(
	private val updateRecordUseCase: UpdateRecordUseCase
) : ActionProcessor<Record.State, Record.Action.RefreshRecord, Record.Effect> {
	override suspend fun process(
		action: Record.Action.RefreshRecord,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return updateRecordUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading ->
						suspend { state: Record.State ->
							when (state) {
								is Record.State.Content -> state
								Record.State.Empty,
								Record.State.Failed,
								Record.State.Idle,
								Record.State.Loading,
								-> Record.State.Loading
							}
						}

					is UseCaseState.Data ->
						suspend { state: Record.State ->
							state
						}

					is UseCaseState.Error ->
						suspend { state: Record.State ->
							if (useCaseState.error == RecordUseCaseError.Unauthorized) {
								sideEffect(Record.Effect.NavigateToOutdatedCredentials)
							}

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
