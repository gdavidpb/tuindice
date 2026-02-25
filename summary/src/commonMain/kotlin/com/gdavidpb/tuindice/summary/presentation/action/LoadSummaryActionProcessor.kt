package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.domain.usecase.GetUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetUserUseCaseError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.mapper.toShortName
import com.gdavidpb.tuindice.summary.presentation.resource.SummaryTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadSummaryActionProcessor(
	private val getUserUseCase: GetUserUseCase,
	private val textProvider: SummaryTextProvider
) : ActionProcessor<Summary.State, Summary.Action.LoadSummary, Summary.Effect>() {

	override fun process(
		action: Summary.Action.LoadSummary,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return getUserUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { _ ->
						Summary.State.Loading
					}

					is UseCaseState.Data -> { _ ->
						with(useCaseState.value) {
							Summary.State.Content(
								name = toShortName(),
								lastUpdate = textProvider.lastUpdate(lastUpdate),
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

					is UseCaseState.Error -> { state ->
						when (val error = useCaseState.error) {
							is GetUserUseCaseError.NoConnection -> {
								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = if (error.isNetworkAvailable)
											textProvider.serviceUnavailable()
										else
											textProvider.networkUnavailable()
									)
								)

								Summary.State.Failed
							}

							is GetUserUseCaseError.OutdatedPassword -> {
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

							is GetUserUseCaseError.Timeout -> {
								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = textProvider.timeout()
									)
								)

								Summary.State.Failed
							}

							is GetUserUseCaseError.Unavailable -> {
								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = textProvider.noService()
									)
								)

								if (state is Summary.State.Content)
									state.copy(
										isUpdated = false
									)
								else
									Summary.State.Failed
							}

							else -> {
								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = textProvider.defaultError()
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
