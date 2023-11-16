package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersError
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadQuartersActionProcessor(
	private val getQuartersUseCase: GetQuartersUseCase,
	private val resourceResolver: ResourceResolver
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
							is GetQuartersError.NoConnection ->
								sideEffect(
									Record.Effect.ShowSnackBar(
										message = if (error.isNetworkAvailable)
											resourceResolver.getString(R.string.snack_service_unavailable)
										else
											resourceResolver.getString(R.string.snack_network_unavailable)
									)
								)

							is GetQuartersError.OutdatedPassword ->
								sideEffect(
									Record.Effect.NavigateToOutdatedPassword
								)

							is GetQuartersError.Timeout ->
								sideEffect(
									Record.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_timeout)
									)
								)

							is GetQuartersError.Unavailable ->
								sideEffect(
									Record.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_service_unavailable)
									)
								)

							else ->
								sideEffect(
									Record.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_default_error)
									)
								)
						}

						Record.State.Failed
					}
				}
			}
	}
}