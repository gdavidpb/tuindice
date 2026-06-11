package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.UpsertAttemptSelectionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.param.UpsertAttemptSelectionParams
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UpsertAttemptSelectionActionProcessor(
	private val upsertAttemptSelectionUseCase: UpsertAttemptSelectionUseCase
) : ActionProcessor<Record.State, Record.Action.UpsertAttemptSelection, Record.Effect> {
	override suspend fun process(
		action: Record.Action.UpsertAttemptSelection,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return upsertAttemptSelectionUseCase.execute(
			UpsertAttemptSelectionParams(
				attemptId = action.attemptId,
				grade = action.grade,
				outcome = action.outcome,
				commit = action.commit
			)
		).map { useCaseState ->
			when (useCaseState) {
				is UseCaseState.Error ->
					suspend { state: Record.State ->
						if (useCaseState.error == RecordUseCaseError.Unauthorized) {
							sideEffect(Record.Effect.NavigateToOutdatedCredentials)
						}
						state
					}

				is UseCaseState.Loading,
				is UseCaseState.Data,
				-> suspend { state: Record.State ->
					state
				}
			}
		}
	}
}
