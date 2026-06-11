package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.usecase.DeleteSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_synthetic_term_delete_failed
import tuindice.record.generated.resources.snack_synthetic_term_deleted

class DeleteSyntheticTermActionProcessor(
	private val deleteSyntheticTermUseCase: DeleteSyntheticTermUseCase
) : ActionProcessor<Record.State, Record.Action.DeleteSyntheticTerm, Record.Effect> {
	override suspend fun process(
		action: Record.Action.DeleteSyntheticTerm,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return deleteSyntheticTermUseCase.execute(action.termId)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading ->
						suspend { state: Record.State ->
							state
						}

					is UseCaseState.Data ->
						suspend { state: Record.State ->
							sideEffect(
								Record.Effect.ShowSnackBar(
									message = getString(Res.string.snack_synthetic_term_deleted)
								)
							)
							state
						}

					is UseCaseState.Error ->
						suspend { state: Record.State ->
							if (useCaseState.error == RecordUseCaseError.Unauthorized) {
								sideEffect(Record.Effect.NavigateToOutdatedCredentials)
							}
							sideEffect(
								Record.Effect.ShowSnackBar(
									message = getString(Res.string.snack_synthetic_term_delete_failed)
								)
							)
							state
						}
				}
			}
	}
}
