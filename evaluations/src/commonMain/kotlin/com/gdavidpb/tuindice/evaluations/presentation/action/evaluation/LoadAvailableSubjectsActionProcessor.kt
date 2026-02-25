package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadAvailableSubjectsActionProcessor(
	private val getAvailableSubjectsUseCase: GetAvailableSubjectsUseCase
) : ActionProcessor<Evaluation.State, Evaluation.Action.LoadAvailableSubjects, Evaluation.Effect>() {

	override fun process(
		action: Evaluation.Action.LoadAvailableSubjects,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return getAvailableSubjectsUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { _ ->
						Evaluation.State.Loading
					}

					is UseCaseState.Data -> { _ ->
						Evaluation.State.Content(
							availableSubjects = useCaseState.value
						)
					}

					is UseCaseState.Error -> { _ ->
						Evaluation.State.Failed
					}
				}
			}
	}
}