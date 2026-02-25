package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersUseCaseError
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.resource.RecordTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadQuartersActionProcessor(
	private val getQuartersUseCase: GetQuartersUseCase,
	private val textProvider: RecordTextProvider
) : ActionProcessor<Record.State, Record.Action.LoadQuarters, Record.Effect>() {

	override fun process(
		action: Record.Action.LoadQuarters,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return getQuartersUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { _ ->
						Record.State.Loading
					}

					is UseCaseState.Data -> { _ ->
						val quarters = useCaseState.value

						if (quarters.isNotEmpty())
							Record.State.Content(quarters)
						else
							Record.State.Empty
					}

					is UseCaseState.Error -> { _ ->
						when (val error = useCaseState.error) {
							is GetQuartersUseCaseError.NoConnection ->
								sideEffect(
									Record.Effect.ShowSnackBar(
										message = if (error.isNetworkAvailable)
											textProvider.serviceUnavailable()
										else
											textProvider.networkUnavailable()
									)
								)

							is GetQuartersUseCaseError.OutdatedPassword ->
								sideEffect(
									Record.Effect.NavigateToOutdatedPassword
								)

							is GetQuartersUseCaseError.Timeout ->
								sideEffect(
									Record.Effect.ShowSnackBar(
										message = textProvider.timeout()
									)
								)

							is GetQuartersUseCaseError.Unavailable ->
								sideEffect(
									Record.Effect.ShowSnackBar(
										message = textProvider.serviceUnavailable()
									)
								)

							else ->
								sideEffect(
									Record.Effect.ShowSnackBar(
										message = textProvider.defaultError()
									)
								)
						}

						Record.State.Failed
					}
				}
			}
	}
}
