package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationItemMapping
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationDateTextMapping
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationFilterGroupItemList
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toEvaluationItemList
import com.gdavidpb.tuindice.evaluations.utils.extension.computeAvailableFilters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.label_state_completed
import tuindice.evaluations.generated.resources.label_state_not_grade
import tuindice.evaluations.generated.resources.label_state_pending

class LoadEvaluationsActionProcessor(
	private val getEvaluationsUseCase: GetEvaluationsUseCase
) : ActionProcessor<Evaluations.State, Evaluations.Action.LoadEvaluations, Evaluations.Effect>() {

	override suspend fun process(
		action: Evaluations.Action.LoadEvaluations,
		sideEffect: (Evaluations.Effect) -> Unit
	): Flow<Mutation<Evaluations.State>> {
		return getEvaluationsUseCase.execute(params = action.activeFilters)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { _ ->
						Evaluations.State.Loading
					}

					is UseCaseState.Data -> suspend { current: Evaluations.State ->
						when (val evaluations = useCaseState.value) {
							GetEvaluations.NoAttempts -> Evaluations.State.NoAttempts

							is GetEvaluations.Content -> {
								val pendingLabel = getString(Res.string.label_state_pending)
								val completedLabel = getString(Res.string.label_state_completed)
								val noGradeLabel = getString(Res.string.label_state_not_grade)
								val dateTextMapping = getEvaluationDateTextMapping()
								val mapping = getEvaluationItemMapping()
								val availableFilters =
									evaluations.originalEvaluations.computeAvailableFilters(
										pendingLabel = pendingLabel,
										completedLabel = completedLabel,
										noGradeLabel = noGradeLabel,
										dateTextMapping = dateTextMapping
									)

								when {
									evaluations.originalEvaluations.isNotEmpty() ->
										Evaluations.State.Content(
											evaluationGroups = evaluations.filteredEvaluations.toEvaluationItemList(
												mapping = mapping
											),
											filterGroups = availableFilters.toEvaluationFilterGroupItemList(
												activeFilters = evaluations.activeFilters
											),
											activeFilters = evaluations.activeFilters
										)

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
							Evaluations.State.Loading,
							-> Evaluations.State.Failed
						}
					}
				}
			}
	}
}
