package com.gdavidpb.tuindice.evaluations.presentation.action.evaluation

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableAttemptsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationGradeSectionItem
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationTypePickerItemList
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationAttemptPickerItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadAvailableAttemptsActionProcessor(
	private val getAvailableAttemptsUseCase: GetAvailableAttemptsUseCase
) : ActionProcessor<Evaluation.State, Evaluation.Action.LoadAvailableAttempts, Evaluation.Effect>() {

	override suspend fun process(
		action: Evaluation.Action.LoadAvailableAttempts,
		sideEffect: (Evaluation.Effect) -> Unit
	): Flow<Mutation<Evaluation.State>> {
		return getAvailableAttemptsUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { _ ->
						Evaluation.State.Loading
					}

					is UseCaseState.Data -> suspend { _ ->
						Evaluation.State.Content(
							attemptItems = useCaseState.value.toEvaluationAttemptPickerItems(
								selectedAttempt = null
							),
							typeItems = getEvaluationTypePickerItemList(
								selectedType = null
							),
							gradeSection = getEvaluationGradeSectionItem(
								isOverdue = false,
								grade = null,
								maxGrade = null
							)
						)
					}

					is UseCaseState.Error -> suspend { _ ->
						Evaluation.State.Failed
					}
				}
			}
	}
}
