package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.model.Evaluation
import com.gdavidpb.tuindice.base.domain.model.EvaluationState
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.academicWeekNumber
import com.gdavidpb.tuindice.evaluations.presentation.mapper.buildEvaluationsWeekItems
import com.gdavidpb.tuindice.evaluations.presentation.mapper.defaultEvaluationsWeekNumber
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationItemMapping
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationDateTextMapping
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationFilterGroupItemList
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationItemList
import com.gdavidpb.tuindice.evaluations.domain.model.EditableAttemptDescriptor
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationTermDescriptor
import com.gdavidpb.tuindice.evaluations.presentation.mapper.EvaluationItemMapping
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsTab
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekGroupItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationsWeekItem
import com.gdavidpb.tuindice.evaluations.utils.extension.computeAvailableFilters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.label_state_completed
import tuindice.evaluations.generated.resources.label_state_not_grade
import tuindice.evaluations.generated.resources.label_state_pending
import tuindice.evaluations.generated.resources.evaluations_week_label

class LoadEvaluationsActionProcessor(
	private val getEvaluationsUseCase: GetEvaluationsUseCase
) : ActionProcessor<Evaluations.State, Evaluations.Action.LoadEvaluations, Evaluations.Effect>() {

	override suspend fun process(
		action: Evaluations.Action.LoadEvaluations,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return getEvaluationsUseCase.execute(params = action.activeFilters)
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
								val pendingLabel = getString(Res.string.label_state_pending)
								val completedLabel = getString(Res.string.label_state_completed)
								val noGradeLabel = getString(Res.string.label_state_not_grade)
								val dateTextMapping = getEvaluationDateTextMapping()
								val mapping = getEvaluationItemMapping()
								val weekLabelPattern = getString(Res.string.evaluations_week_label)
								val availableFilters =
									evaluations.originalEvaluations.computeAvailableFilters(
										pendingLabel = pendingLabel,
										completedLabel = completedLabel,
										noGradeLabel = noGradeLabel,
										dateTextMapping = dateTextMapping
									)

								when {
									evaluations.originalEvaluations.isNotEmpty() ->
										{
											val selectedTab = when (current) {
												is Evaluations.State.Content -> current.selectedTab
												else -> EvaluationsTab.Upcoming
											}
											val defaultWeekNumber = defaultEvaluationsWeekNumber(
												currentTerm = evaluations.displayContext.currentTerm
											)
											val selectedWeekNumber = when (current) {
												is Evaluations.State.Content -> current.selectedWeekNumber
												else -> defaultWeekNumber
											}.coerceIn(MIN_WEEK_NUMBER, MAX_WEEK_NUMBER)
											val weekItems = buildEvaluationsWeekItems(
												currentTerm = evaluations.displayContext.currentTerm,
												evaluations = evaluations.filteredEvaluations,
												weekLabelPattern = weekLabelPattern
											)
											val weeklyUpcomingGroups = evaluations.filteredEvaluations
												.toWeeklyEvaluationGroups(
													currentTerm = evaluations.displayContext.currentTerm,
													attempts = evaluations.displayContext.attempts,
													mapping = mapping
												) { evaluation ->
													evaluation.state == EvaluationState.PENDING ||
														evaluation.state == EvaluationState.OVERDUE
												}
											val weeklyHistoryGroups = evaluations.filteredEvaluations
												.toWeeklyEvaluationGroups(
													currentTerm = evaluations.displayContext.currentTerm,
													attempts = evaluations.displayContext.attempts,
													mapping = mapping
												) { evaluation ->
													evaluation.state == EvaluationState.COMPLETED ||
														evaluation.state == EvaluationState.CONTINUOUS
												}
											val upcomingWeekGroups = weekItems.toEvaluationsWeekGroupItems(
												weeklyGroups = weeklyUpcomingGroups
											)
											val historyWeekGroups = weekItems.toEvaluationsWeekGroupItems(
												weeklyGroups = weeklyHistoryGroups
											)
											val upcomingGroups = upcomingWeekGroups.flatMap { weekGroup ->
												weekGroup.groups
											}
											val historyGroups = historyWeekGroups.flatMap { weekGroup ->
												weekGroup.groups
											}

											Evaluations.State.Content(
												selectedTab = selectedTab,
												upcomingGroups = upcomingGroups,
												historyGroups = historyGroups,
												weekItem = weekItems.first { item ->
													item.weekNumber == selectedWeekNumber
												},
												weekItems = weekItems,
												selectedWeekNumber = selectedWeekNumber,
												weeklyUpcomingGroups = weeklyUpcomingGroups,
												weeklyHistoryGroups = weeklyHistoryGroups,
												upcomingWeekGroups = upcomingWeekGroups,
												historyWeekGroups = historyWeekGroups,
												evaluationWeekGroups = when (selectedTab) {
													EvaluationsTab.Upcoming -> upcomingWeekGroups
													EvaluationsTab.History -> historyWeekGroups
												},
												evaluationGroups = when (selectedTab) {
													EvaluationsTab.Upcoming -> upcomingGroups
													EvaluationsTab.History -> historyGroups
												},
												filterGroups = availableFilters.toEvaluationFilterGroupItemList(
													activeFilters = evaluations.activeFilters
												),
												activeFilters = evaluations.activeFilters
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

private fun List<EvaluationsWeekItem>.toEvaluationsWeekGroupItems(
	weeklyGroups: Map<Int, List<EvaluationsGroupItem>>
): List<EvaluationsWeekGroupItem> {
	return map { weekItem ->
		EvaluationsWeekGroupItem(
			weekNumber = weekItem.weekNumber,
			title = weekItem.labelText,
			groups = weeklyGroups[weekItem.weekNumber] ?: emptyList()
		)
	}
}

private fun List<Evaluation>.toWeeklyEvaluationGroups(
	currentTerm: EvaluationTermDescriptor?,
	attempts: List<EditableAttemptDescriptor>,
	mapping: EvaluationItemMapping,
	predicate: (Evaluation) -> Boolean
): Map<Int, List<EvaluationsGroupItem>> {
	return (MIN_WEEK_NUMBER..MAX_WEEK_NUMBER).associateWith { weekNumber ->
		filter(predicate)
			.filter { evaluation -> evaluation.academicWeekNumber(currentTerm) == weekNumber }
			.toEvaluationItemList(
				mapping = mapping,
				attempts = attempts
			)
	}
}

private const val MIN_WEEK_NUMBER = 1
private const val MAX_WEEK_NUMBER = 12
