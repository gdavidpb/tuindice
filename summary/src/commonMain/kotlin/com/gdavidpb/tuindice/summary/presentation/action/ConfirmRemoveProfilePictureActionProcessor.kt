package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.resource.SummaryTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ConfirmRemoveProfilePictureActionProcessor(
	private val removeProfilePictureUseCase: RemoveProfilePictureUseCase,
	private val textProvider: SummaryTextProvider
) : ActionProcessor<Summary.State, Summary.Action.ConfirmRemoveProfilePicture, Summary.Effect>() {

	override fun process(
		action: Summary.Action.ConfirmRemoveProfilePicture,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return removeProfilePictureUseCase.execute(Unit)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> { state ->
						if (state is Summary.State.Content)
							state.copy(
								isProfilePictureLoading = true
							)
						else
							state
					}

					is UseCaseState.Data -> { state ->
						if (state is Summary.State.Content) {
							sideEffect(
								Summary.Effect.ShowSnackBar(
									message = textProvider.profilePictureRemoved()
								)
							)

							state.copy(
								profilePictureUrl = "",
								isProfilePictureLoading = false
							)
						} else
							state
					}

					is UseCaseState.Error -> { state ->
						if (state is Summary.State.Content) {
							when (val error = useCaseState.error) {
								is ProfilePictureUseCaseError.Timeout ->
									sideEffect(
										Summary.Effect.ShowSnackBar(
											message = textProvider.timeout()
										)
									)

								is ProfilePictureUseCaseError.NoConnection ->
									sideEffect(
										Summary.Effect.ShowSnackBar(
											message = if (error.isNetworkAvailable)
												textProvider.serviceUnavailable()
											else
												textProvider.networkUnavailable()
										)
									)

								else ->
									sideEffect(
										Summary.Effect.ShowSnackBar(
											message = textProvider.defaultError()
										)
									)
							}

							state.copy(
								isProfilePictureLoading = false
							)
						} else
							state
					}
				}
			}
	}
}
