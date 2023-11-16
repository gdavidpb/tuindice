package com.gdavidpb.tuindice.evaluations.presentation.action.add

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadAvailableSubjectsActionProcessor(
	private val getAvailableSubjectsUseCase: GetAvailableSubjectsUseCase
) : ActionProcessor<AddEvaluation.State, AddEvaluation.Action.LoadAvailableSubjects, AddEvaluation.Effect>() {

	override fun process(
		action: AddEvaluation.Action.LoadAvailableSubjects,
		sideEffect: (AddEvaluation.Effect) -> Unit
	): Flow<Mutation<AddEvaluation.State>> {
		return getAvailableSubjectsUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { _ ->
						AddEvaluation.State.Loading
					}

					is UseCaseState.Data -> { _ ->
						AddEvaluation.State.Content(
							availableSubjects = useCaseState.value
						)
					}

					is UseCaseState.Error -> { _ ->
						AddEvaluation.State.Failed
					}
				}
			}
	}
}