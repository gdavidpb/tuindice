package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.usecase.UpdateQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.error.UpdateQuartersUseCaseError
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_default_error
import tuindice.record.generated.resources.snack_network_unavailable
import tuindice.record.generated.resources.snack_service_unavailable
import tuindice.record.generated.resources.snack_timeout

class RefreshQuartersActionProcessor(
	private val updateQuartersUseCase: UpdateQuartersUseCase
) : ActionProcessor<Record.State, Record.Action.RefreshQuarters, Record.Effect>() {

	override suspend fun process(
		action: Record.Action.RefreshQuarters,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return updateQuartersUseCase.execute(Unit)
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { state: Record.State ->
						when (state) {
							is Record.State.Content -> state
							Record.State.Empty,
							Record.State.Failed,
							Record.State.Loading,
							-> Record.State.Loading
						}
					}

					is UseCaseState.Data -> null

					is UseCaseState.Error -> suspend { state: Record.State ->
						val message = when (val error = useCaseState.error) {
							is UpdateQuartersUseCaseError.NoConnection ->
								if (error.isNetworkAvailable)
									getString(Res.string.snack_service_unavailable)
								else
									getString(Res.string.snack_network_unavailable)

							is UpdateQuartersUseCaseError.Timeout ->
								getString(Res.string.snack_timeout)

							is UpdateQuartersUseCaseError.Unavailable ->
								getString(Res.string.snack_service_unavailable)

							else ->
								getString(Res.string.snack_default_error)
						}

						sideEffect(
							Record.Effect.ShowSnackBar(
								message = message
							)
						)

						when (state) {
							is Record.State.Content -> state

							Record.State.Empty,
							Record.State.Failed,
							Record.State.Loading,
							-> Record.State.Failed
						}
					}
				}
			}
	}
}
