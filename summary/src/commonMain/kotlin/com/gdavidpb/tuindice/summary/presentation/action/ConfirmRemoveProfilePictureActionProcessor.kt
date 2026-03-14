package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.getString
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.snack_default_error
import tuindice.summary.generated.resources.snack_network_unavailable
import tuindice.summary.generated.resources.snack_profile_picture_removed
import tuindice.summary.generated.resources.snack_service_unavailable
import tuindice.summary.generated.resources.snack_timeout

class ConfirmRemoveProfilePictureActionProcessor(
	private val removeProfilePictureUseCase: RemoveProfilePictureUseCase
) : ActionProcessor<Summary.State, Summary.Action.ConfirmRemoveProfilePicture, Summary.Effect>() {

	override suspend fun process(
		action: Summary.Action.ConfirmRemoveProfilePicture,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return removeProfilePictureUseCase.execute(Unit)
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
						val successMessage = getString(Res.string.snack_profile_picture_removed)

						if (state is Summary.State.Content) {
							sideEffect(
								Summary.Effect.ShowSnackBar(
									message = successMessage
								)
							)

							state.copy(
								profilePictureUrl = "",
								isProfilePictureLoading = false
							)
						} else
							state
					}

					is UseCaseState.Error -> suspend { state: Summary.State ->
						val message = when (val error = useCaseState.error) {
							ProfilePictureUseCaseError.NotFound ->
								getString(Res.string.snack_profile_picture_removed)

							is ProfilePictureUseCaseError.Timeout ->
								getString(Res.string.snack_timeout)

							is ProfilePictureUseCaseError.NoConnection ->
								if (error.isNetworkAvailable)
									getString(Res.string.snack_service_unavailable)
								else
									getString(Res.string.snack_network_unavailable)

							else ->
								getString(Res.string.snack_default_error)
						}

						if (state is Summary.State.Content) {
							sideEffect(
								Summary.Effect.ShowSnackBar(
									message = message
								)
							)

							if (useCaseState.error == ProfilePictureUseCaseError.NotFound)
								state.copy(
									profilePictureUrl = "",
									isProfilePictureLoading = false
								)
							else
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
