package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.presentation.model.SyncedContentResolution
import com.gdavidpb.tuindice.base.presentation.model.resolveSyncedContentResolution
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.buildEvaluationsWeekItems
import com.gdavidpb.tuindice.evaluations.presentation.mapper.defaultEvaluationsWeekKey
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationItemMapping
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationsWeekGroupItemList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluations_continuous_label
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

							is GetEvaluations.Content ->
								when (
									resolveSyncedContentResolution(
										hasContent = evaluations.evaluations.isNotEmpty(),
										hasSynced = evaluations.hasSyncedEvaluations,
										keepCurrentWhileWaiting = current is Evaluations.State.Failed
									)
								) {
									SyncedContentResolution.Content ->
										evaluations.toContentState(current)

									SyncedContentResolution.Empty ->
										Evaluations.State.Empty

									SyncedContentResolution.Loading ->
										Evaluations.State.Loading

									SyncedContentResolution.KeepCurrent ->
										current
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

private suspend fun GetEvaluations.Content.toContentState(current: Evaluations.State): Evaluations.State {
	val mapping = getEvaluationItemMapping()
	val weekLabelPattern = getString(Res.string.evaluations_week_label)
	val continuousLabel = getString(Res.string.evaluations_continuous_label)
	val defaultWeekKey = defaultEvaluationsWeekKey(
		currentTerm = displayContext.currentTerm,
		evaluations = evaluations
	)
	val weekItems = buildEvaluationsWeekItems(
		currentTerm = displayContext.currentTerm,
		evaluations = evaluations,
		weekLabelPattern = weekLabelPattern,
		continuousLabel = continuousLabel
	)
	val selectedWeekKey = when (current) {
		is Evaluations.State.Content -> current.selectedWeekKey
		else -> defaultWeekKey
	}.takeIf { key ->
		weekItems.any { item -> item.key == key }
	} ?: defaultWeekKey.takeIf { key ->
		weekItems.any { item -> item.key == key }
	} ?: weekItems.first().key
	val evaluationWeekGroups = weekItems.toEvaluationsWeekGroupItemList(
		evaluations = evaluations,
		currentTerm = displayContext.currentTerm,
		attempts = displayContext.attempts,
		mapping = mapping
	)
	val evaluationGroups = evaluationWeekGroups.flatMap { weekGroup ->
		weekGroup.groups
	}

	return Evaluations.State.Content(
		weekItem = weekItems.first { item ->
			item.key == selectedWeekKey
		},
		weekItems = weekItems,
		selectedWeekKey = selectedWeekKey,
		evaluationGroups = evaluationGroups,
		evaluationWeekGroups = evaluationWeekGroups
	)
}
