package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationsUseCaseError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.snack_default_error
import tuindice.evaluations.generated.resources.snack_network_unavailable
import tuindice.evaluations.generated.resources.snack_service_unavailable
import tuindice.evaluations.generated.resources.snack_timeout

class RefreshEvaluationsActionProcessor(
	private val updateEvaluationsUseCase: UpdateEvaluationsUseCase
) : ActionProcessor<Evaluations.State, Evaluations.Action.RefreshEvaluations, Evaluations.Effect>() {
	override suspend fun process(
		action: Evaluations.Action.RefreshEvaluations,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return updateEvaluationsUseCase.execute(Unit)
			.mapNotNull { state ->
				when (state) {
					is UseCaseState.Loading -> suspend { current: Evaluations.State ->
						when (current) {
							is Evaluations.State.Content -> current
							Evaluations.State.Empty,
							Evaluations.State.Failed,
							Evaluations.State.Loading,
							Evaluations.State.NoAttempts,
							-> Evaluations.State.Loading
						}
					}

					is UseCaseState.Data -> null

					is UseCaseState.Error -> suspend { current: Evaluations.State ->
						val message = when (val error = state.error) {
							is UpdateEvaluationsUseCaseError.NoConnection ->
								if (error.isNetworkAvailable) {
									getString(Res.string.snack_service_unavailable)
								} else {
									getString(Res.string.snack_network_unavailable)
								}

							is UpdateEvaluationsUseCaseError.Timeout ->
								getString(Res.string.snack_timeout)

							is UpdateEvaluationsUseCaseError.Unavailable ->
								getString(Res.string.snack_service_unavailable)

							else ->
								getString(Res.string.snack_default_error)
						}

						sideEffect(
							Evaluations.Effect.ShowSnackBar(message = message)
						)

						when (current) {
							is Evaluations.State.Content -> current
							Evaluations.State.Empty,
							Evaluations.State.Failed,
							Evaluations.State.Loading,
							Evaluations.State.NoAttempts,
							-> Evaluations.State.Failed
						}
					}
				}
			}
	}
}
