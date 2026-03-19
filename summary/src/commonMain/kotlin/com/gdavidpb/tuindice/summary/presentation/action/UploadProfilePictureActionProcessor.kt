package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.snack_default_error
import tuindice.summary.generated.resources.snack_network_unavailable
import tuindice.summary.generated.resources.snack_profile_picture_not_image
import tuindice.summary.generated.resources.snack_profile_picture_size_exceeded
import tuindice.summary.generated.resources.snack_profile_picture_updated
import tuindice.summary.generated.resources.snack_service_unavailable
import tuindice.summary.generated.resources.snack_timeout

class UploadProfilePictureActionProcessor(
	private val uploadProfilePictureUseCase: UploadProfilePictureUseCase
) : ActionProcessor<Summary.State, Summary.Action.UploadProfilePicture, Summary.Effect>() {

	override suspend fun process(
		action: Summary.Action.UploadProfilePicture,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return uploadProfilePictureUseCase.execute(params = action.file)
			.map { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { state ->
						if (state is Summary.State.Content)
							state.copy(
								isProfilePictureLoading = true
							)
						else
							state
					}

					is UseCaseState.Data -> suspend { state: Summary.State ->
						val successMessage = getString(Res.string.snack_profile_picture_updated)

						if (state is Summary.State.Content) {
							sideEffect(
								Summary.Effect.ShowSnackBar(
									message = successMessage
								)
							)

							state.copy(
								isProfilePictureLoading = false
							)
						} else
							state
					}

					is UseCaseState.Error -> suspend { state: Summary.State ->
						val message = when (val error = useCaseState.error) {
							is ProfilePictureUseCaseError.Timeout ->
								getString(Res.string.snack_timeout)

							is ProfilePictureUseCaseError.NoConnection ->
								if (error.isNetworkAvailable)
									getString(Res.string.snack_service_unavailable)
								else
									getString(Res.string.snack_network_unavailable)

							ProfilePictureUseCaseError.NotImage ->
								getString(Res.string.snack_profile_picture_not_image)

							ProfilePictureUseCaseError.SizeExceeded ->
								getString(Res.string.snack_profile_picture_size_exceeded)

							else ->
								getString(Res.string.snack_default_error)
						}

						sideEffect(
							Summary.Effect.ShowSnackBar(
								message = message
							)
						)

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
