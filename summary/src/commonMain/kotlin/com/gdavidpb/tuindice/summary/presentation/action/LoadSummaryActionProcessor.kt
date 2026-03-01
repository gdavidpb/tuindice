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
					is UseCaseState.Loading -> { _ ->
						Summary.State.Loading
					}

					is UseCaseState.Data -> run {
						val user = useCaseState.value
						val lastUpdateText = getString(
							Res.string.text_last_update,
							user.lastUpdate.formatLastUpdate()
						)

						suspend { _: Summary.State ->
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
									isLoading = false,
									isUpdated = true,
									isUpdating = false
								)
							}
						}
					}

					is UseCaseState.Error -> when (val error = useCaseState.error) {
						is GetUserUseCaseError.NoConnection -> run {
							val message = if (error.isNetworkAvailable)
								getString(Res.string.snack_service_unavailable)
							else
								getString(Res.string.snack_network_unavailable)

							suspend { _: Summary.State ->
								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = message
									)
								)

								Summary.State.Failed
							}
						}

						is GetUserUseCaseError.OutdatedPassword -> run {
							suspend { state: Summary.State ->
								if (state is Summary.State.Content) {
									sideEffect(
										Summary.Effect.NavigateToOutdatedPassword
									)

									state.copy(
										isUpdating = false
									)
								} else
									Summary.State.Failed
							}
						}

						is GetUserUseCaseError.Timeout -> run {
							val message = getString(Res.string.snack_timeout)

							suspend { _: Summary.State ->
								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = message
									)
								)

								Summary.State.Failed
							}
						}

						is GetUserUseCaseError.Unavailable -> run {
							val message = getString(Res.string.snack_no_service)

							suspend { state: Summary.State ->
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
						}

						else -> run {
							val message = getString(Res.string.snack_default_error)

							suspend { _: Summary.State ->
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
}
