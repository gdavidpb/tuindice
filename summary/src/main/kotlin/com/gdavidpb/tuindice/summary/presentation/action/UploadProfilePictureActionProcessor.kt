package com.gdavidpb.tuindice.summary.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.utils.ResourceResolver
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.domain.usecase.TakeProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UploadProfilePictureActionProcessor(
	private val takeProfilePictureUseCase: TakeProfilePictureUseCase,
	private val resourceResolver: ResourceResolver
) : ActionProcessor<Summary.State, Summary.Action.UploadProfilePicture, Summary.Effect>() {

	override fun process(
		action: Summary.Action.UploadProfilePicture,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return takeProfilePictureUseCase.execute(Unit)
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
									message = resourceResolver.getString(R.string.snack_profile_picture_updated)
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
							is ProfilePictureError.Timeout ->
								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_timeout)
									)
								)

							is ProfilePictureError.NoConnection ->
								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = if (error.isNetworkAvailable)
											resourceResolver.getString(R.string.snack_service_unavailable)
										else
											resourceResolver.getString(R.string.snack_network_unavailable)
									)
								)

							else ->
								sideEffect(
									Summary.Effect.ShowSnackBar(
										message = resourceResolver.getString(R.string.snack_default_error)
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