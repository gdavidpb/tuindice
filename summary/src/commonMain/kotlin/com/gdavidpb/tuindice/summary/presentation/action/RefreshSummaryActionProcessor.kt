package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateUserUseCaseError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import org.jetbrains.compose.resources.getString
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.snack_default_error
import tuindice.summary.generated.resources.snack_network_unavailable
import tuindice.summary.generated.resources.snack_no_service
import tuindice.summary.generated.resources.snack_service_unavailable
import tuindice.summary.generated.resources.snack_timeout

class RefreshSummaryActionProcessor(
	private val updateUserUseCase: UpdateUserUseCase
) : ActionProcessor<Summary.State, Summary.Action.RefreshSummary, Summary.Effect>() {

	override suspend fun process(
		action: Summary.Action.RefreshSummary,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return updateUserUseCase.execute(Unit)
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading,
					is UseCaseState.Data -> null

					is UseCaseState.Error -> when (val error = useCaseState.error) {
						is UpdateUserUseCaseError.NoConnection -> {
							suspend { _: Summary.State ->
								val message = if (error.isNetworkAvailable)
									getString(Res.string.snack_service_unavailable)
								else
									getString(Res.string.snack_network_unavailable)

								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = message
									)
								)

								Summary.State.Failed
							}
						}

						is UpdateUserUseCaseError.Timeout -> {
							suspend { _: Summary.State ->
								val message = getString(Res.string.snack_timeout)

								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = message
									)
								)

								Summary.State.Failed
							}
						}

						is UpdateUserUseCaseError.Unavailable,
						UpdateUserUseCaseError.NotFound -> {
							suspend { state: Summary.State ->
								val message = getString(Res.string.snack_no_service)

								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = message
									)
								)

								if (state is Summary.State.Content)
									state
								else
									Summary.State.Failed
							}
						}

						else -> {
							suspend { _: Summary.State ->
								val message = getString(Res.string.snack_default_error)

								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = message
									)
								)

								Summary.State.Failed
							}
						}
					}
				}
			}
			.onStart {
				emit { state ->
					if (state is Summary.State.Content)
						state
					else
						Summary.State.Loading
				}
			}
	}
}
