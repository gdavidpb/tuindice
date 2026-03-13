package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersUseCaseError
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_default_error
import tuindice.record.generated.resources.snack_network_unavailable
import tuindice.record.generated.resources.snack_service_unavailable
import tuindice.record.generated.resources.snack_timeout

class LoadQuartersActionProcessor(
	private val getQuartersUseCase: GetQuartersUseCase
) : ActionProcessor<Record.State, Record.Action.LoadQuarters, Record.Effect>() {

	override suspend fun process(
		action: Record.Action.LoadQuarters,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return getQuartersUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { _ ->
						Record.State.Loading
					}

					is UseCaseState.Data -> suspend { _ ->
						val quarters = useCaseState.value

						if (quarters.isNotEmpty())
							Record.State.Content(quarters)
						else
							Record.State.Empty
					}

					is UseCaseState.Error -> when (val error = useCaseState.error) {
						is GetQuartersUseCaseError.NoConnection -> suspend { _: Record.State ->
							val message = if (error.isNetworkAvailable)
								getString(Res.string.snack_service_unavailable)
							else
								getString(Res.string.snack_network_unavailable)

							sideEffect(
								Record.Effect.ShowSnackBar(
									message = message
								)
							)

							Record.State.Failed
						}

						is GetQuartersUseCaseError.Timeout -> suspend { _: Record.State ->
							val message = getString(Res.string.snack_timeout)

							sideEffect(
								Record.Effect.ShowSnackBar(
									message = message
								)
							)

							Record.State.Failed
						}

						is GetQuartersUseCaseError.Unavailable -> suspend { _: Record.State ->
							val message = getString(Res.string.snack_service_unavailable)

							sideEffect(
								Record.Effect.ShowSnackBar(
									message = message
								)
							)

							Record.State.Failed
						}

						else -> suspend { _: Record.State ->
							val message = getString(Res.string.snack_default_error)

							sideEffect(
								Record.Effect.ShowSnackBar(
									message = message
								)
							)

							Record.State.Failed
						}
					}
				}
			}
	}
}
