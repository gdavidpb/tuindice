package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.domain.usecase.GetUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetUserUseCaseError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.mapper.formatLastUpdate
import com.gdavidpb.tuindice.summary.presentation.mapper.toShortName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.snack_default_error
import tuindice.summary.generated.resources.snack_network_unavailable
import tuindice.summary.generated.resources.snack_no_service
import tuindice.summary.generated.resources.snack_service_unavailable
import tuindice.summary.generated.resources.snack_timeout
import tuindice.summary.generated.resources.text_last_update

class LoadSummaryActionProcessor(
	private val getUserUseCase: GetUserUseCase
) : ActionProcessor<Summary.State, Summary.Action.LoadSummary, Summary.Effect>() {

	override suspend fun process(
		action: Summary.Action.LoadSummary,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return getUserUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { _ ->
						Summary.State.Loading
					}

					is UseCaseState.Data -> suspend { _: Summary.State ->
						val user = useCaseState.value
						val lastUpdateText = getString(
							Res.string.text_last_update,
							user.lastUpdate.formatLastUpdate()
						)

						with(user) {
							Summary.State.Content(
								name = toShortName(),
								lastUpdate = lastUpdateText,
								careerName = careerName,
								grade = grade.toFloat(),
								enrolledSubjects = enrolledSubjects,
								enrolledCredits = enrolledCredits,
								approvedSubjects = approvedSubjects,
								approvedCredits = approvedCredits,
								retiredSubjects = retiredSubjects,
								retiredCredits = retiredCredits,
								failedSubjects = failedSubjects,
								failedCredits = failedCredits,
								profilePictureUrl = pictureUrl,
								isGradeVisible = (grade > 0.0),
								isProfilePictureLoading = false,
								isUpdated = true,
								isUpdating = false
							)
						}
					}

					is UseCaseState.Error -> when (val error = useCaseState.error) {
						is GetUserUseCaseError.NoConnection -> suspend { _: Summary.State ->
							val message = if (error.isNetworkAvailable)
								getString(Res.string.snack_service_unavailable)
							else
								getString(Res.string.snack_network_unavailable)

							sideEffect(
								Summary.Effect.ShowSnackBar(
									message = message
								)
							)

							Summary.State.Failed
						}

						is GetUserUseCaseError.Timeout -> suspend { _: Summary.State ->
							val message = getString(Res.string.snack_timeout)

							sideEffect(
								Summary.Effect.ShowSnackBar(
									message = message
								)
							)

							Summary.State.Failed
						}

						is GetUserUseCaseError.Unavailable -> suspend { state: Summary.State ->
							val message = getString(Res.string.snack_no_service)

							sideEffect(
								Summary.Effect.ShowSnackBar(
									message = message
								)
							)

							if (state is Summary.State.Content)
								state.copy(
									isUpdated = false
								)
							else
								Summary.State.Failed
						}

						else -> suspend { _: Summary.State ->
							val message = getString(Res.string.snack_default_error)

							sideEffect(
								Summary.Effect.ShowSnackBar(
									message = message
								)
							)

							Summary.State.Failed
						}
					}
				}
			}
	}
}
