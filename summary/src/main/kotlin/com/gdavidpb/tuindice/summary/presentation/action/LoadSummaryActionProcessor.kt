package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.domain.usecase.GetAccountUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetAccountError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.mapper.formatLastUpdate
import com.gdavidpb.tuindice.summary.presentation.mapper.toShortName
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LoadSummaryActionProcessor(
	private val getAccountUseCase: GetAccountUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<Summary.State, Summary.Action.LoadSummary, Summary.Effect>() {

	override fun process(
		action: Summary.Action.LoadSummary,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return getAccountUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { _ ->
						Summary.State.Loading
					}

					is UseCaseState.Data -> { _ ->
						with(useCaseState.value) {
							Summary.State.Content(
								name = toShortName(),
								lastUpdate = resourceResolver.getString(
									R.string.text_last_update,
									lastUpdate.formatLastUpdate()
								),
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
							is GetAccountError.NoConnection -> {
								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = if (error.isNetworkAvailable)
											resourceResolver.getString(R.string.snack_service_unavailable)
										else
											resourceResolver.getString(R.string.snack_network_unavailable)
									)
								)

								Summary.State.Failed
							}

							is GetAccountError.OutdatedPassword -> {
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

							is GetAccountError.Timeout -> {
								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_timeout)
									)
								)

								Summary.State.Failed
							}

							is GetAccountError.Unavailable -> {
								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_no_service)
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
										message = resourceResolver.getString(R.string.snack_default_error)
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