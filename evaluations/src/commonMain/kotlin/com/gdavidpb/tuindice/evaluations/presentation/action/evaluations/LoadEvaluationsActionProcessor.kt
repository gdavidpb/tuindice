package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.buildEvaluationsWeekItems
import com.gdavidpb.tuindice.evaluations.presentation.mapper.defaultEvaluationsWeekNumber
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationItemMapping
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationsWeekGroupItemList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluations_week_label

class LoadEvaluationsActionProcessor(
	private val getEvaluationsUseCase: GetEvaluationsUseCase
) : ActionProcessor<Evaluations.State, Evaluations.Action.LoadEvaluations, Evaluations.Effect>() {

	override suspend fun process(
		action: Evaluations.Action.LoadEvaluations,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return getEvaluationsUseCase.execute(params = Unit)
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> null

					is UseCaseState.Data -> suspend { current: Evaluations.State ->
						when (val evaluations = useCaseState.value) {
							GetEvaluations.WaitingForRecordData ->
								when (current) {
									is Evaluations.State.Failed -> current
									else -> Evaluations.State.Loading
								}

							GetEvaluations.RecordDataUnavailable ->
								Evaluations.State.Failed

							GetEvaluations.NoAttempts -> Evaluations.State.NoAttempts

							is GetEvaluations.Content -> {
								val mapping = getEvaluationItemMapping()
								val weekLabelPattern = getString(Res.string.evaluations_week_label)

								when {
									evaluations.evaluations.isNotEmpty() ->
										{
											val defaultWeekNumber = defaultEvaluationsWeekNumber(
												currentTerm = evaluations.displayContext.currentTerm
											)
											val selectedWeekNumber = when (current) {
												is Evaluations.State.Content -> current.selectedWeekNumber
												else -> defaultWeekNumber
											}.coerceIn(MIN_WEEK_NUMBER, MAX_WEEK_NUMBER)
											val weekItems = buildEvaluationsWeekItems(
												currentTerm = evaluations.displayContext.currentTerm,
												evaluations = evaluations.evaluations,
												weekLabelPattern = weekLabelPattern
											)
											val evaluationWeekGroups = weekItems.toEvaluationsWeekGroupItemList(
												evaluations = evaluations.evaluations,
												currentTerm = evaluations.displayContext.currentTerm,
												attempts = evaluations.displayContext.attempts,
												mapping = mapping
											)
											val evaluationGroups = evaluationWeekGroups.flatMap { weekGroup ->
												weekGroup.groups
											}

											Evaluations.State.Content(
												weekItem = weekItems.first { item ->
													item.weekNumber == selectedWeekNumber
												},
												weekItems = weekItems,
												selectedWeekNumber = selectedWeekNumber,
												evaluationGroups = evaluationGroups,
												evaluationWeekGroups = evaluationWeekGroups
											)
										}

									evaluations.hasSyncedEvaluations ->
										Evaluations.State.Empty

									else ->
										when (current) {
											is Evaluations.State.Failed -> current
											else -> Evaluations.State.Loading
										}
								}
							}
						}
					}

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

private const val MIN_WEEK_NUMBER = 1
private const val MAX_WEEK_NUMBER = 12
