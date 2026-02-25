package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.resource.SummaryTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UploadProfilePictureActionProcessor(
	private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
	private val textProvider: SummaryTextProvider
) : ActionProcessor<Summary.State, Summary.Action.UploadProfilePicture, Summary.Effect>() {

	override fun process(
		action: Summary.Action.UploadProfilePicture,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return uploadProfilePictureUseCase.execute(params = action.uri)
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
									message = textProvider.profilePictureUpdated()
								)
							)

							state.copy(
								profilePictureUrl = useCaseState.value,
								isProfilePictureLoading = false
							)
						} else
							state
					}

					is UseCaseState.Error -> { state ->
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

						if (state is Summary.State.Content)
							state.copy(
								isProfilePictureLoading = false
							)
						else
							state
					}
				}
			}
	}
}
