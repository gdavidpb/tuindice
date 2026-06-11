package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class RefreshEvaluationsActionProcessor(
	private val updateEvaluationsUseCase: UpdateEvaluationsUseCase
) : ActionProcessor<Evaluations.State, Evaluations.Action.RefreshEvaluations, Evaluations.Effect> {
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
							Evaluations.State.Empty -> current
							Evaluations.State.NoAttempts -> current
							Evaluations.State.Failed,
							Evaluations.State.Idle,
							Evaluations.State.Loading,
							-> Evaluations.State.Loading
						}
					}

					is UseCaseState.Data -> null

					is UseCaseState.Error -> suspend { current: Evaluations.State ->
						when (current) {
							is Evaluations.State.Content -> current
							Evaluations.State.Empty -> current
							Evaluations.State.NoAttempts -> current
							Evaluations.State.Failed,
							Evaluations.State.Idle,
							Evaluations.State.Loading,
							-> Evaluations.State.Failed
						}
					}
				}
			}
	}
}
