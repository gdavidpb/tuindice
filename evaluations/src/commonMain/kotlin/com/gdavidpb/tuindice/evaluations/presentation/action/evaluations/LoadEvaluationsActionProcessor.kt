package com.gdavidpb.tuindice.evaluations.presentation.action.evaluations

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetEvaluationsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.mapper.getEvaluationDateTextMapping
import com.gdavidpb.tuindice.evaluations.utils.extension.computeAvailableFilters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.label_state_completed
import tuindice.evaluations.generated.resources.label_state_not_grade
import tuindice.evaluations.generated.resources.label_state_pending
import tuindice.evaluations.generated.resources.snack_default_error
import tuindice.evaluations.generated.resources.snack_network_unavailable
import tuindice.evaluations.generated.resources.snack_service_unavailable
import tuindice.evaluations.generated.resources.snack_timeout

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
							GetEvaluations.NoSubjects -> Evaluations.State.NoSubjects

							is GetEvaluations.Content -> {
								val pendingLabel = getString(Res.string.label_state_pending)
								val completedLabel = getString(Res.string.label_state_completed)
								val noGradeLabel = getString(Res.string.label_state_not_grade)
								val dateTextMapping = getEvaluationDateTextMapping()
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
											originalEvaluations = evaluations.originalEvaluations,
											filteredEvaluations = evaluations.filteredEvaluations,
											availableFilters = availableFilters,
											activeFilters = evaluations.activeFilters
										)

									current is Evaluations.State.Loading ->
										current

									else ->
										Evaluations.State.Empty
								}
							}
						}
					}

					is UseCaseState.Error -> suspend { _: Evaluations.State ->
						val error = useCaseState.error
						val message = when (error) {
							is EvaluationsUseCaseError.NoConnection ->
								if (error.isNetworkAvailable)
									getString(Res.string.snack_service_unavailable)
								else
									getString(Res.string.snack_network_unavailable)

							is EvaluationsUseCaseError.Timeout ->
								getString(Res.string.snack_timeout)

							is EvaluationsUseCaseError.Unavailable ->
								getString(Res.string.snack_service_unavailable)

							else ->
								getString(Res.string.snack_default_error)
						}

						sideEffect(
							Evaluations.Effect.ShowSnackBar(
								message = message
							)
						)

						Evaluations.State.Failed
					}
				}
			}
	}
}
