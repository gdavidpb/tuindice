package com.gdavidpb.tuindice.summary.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary

class UploadProfilePictureReducer(
	override val useCase: UploadProfilePictureUseCase
) : BaseReducer<String, String, ProfilePictureError, Summary.State, Summary.Action.UploadProfilePicture, Summary.Event>() {
	override fun actionToParams(action: Summary.Action.UploadProfilePicture): String {
		return action.path
	}

	override suspend fun reduceLoadingState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Loading<String, ProfilePictureError>,
		eventProducer: (Summary.Event) -> Unit
	): Summary.State? {
		return if (currentState is Summary.State.Loaded) {
			val newState = currentState.value.copy(
				isProfilePictureLoading = true
			)

			Summary.State.Loaded(newState)
		} else {
			super.reduceLoadingState(currentState, useCaseState, eventProducer)
		}
	}

	override suspend fun reduceDataState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Data<String, ProfilePictureError>,
		eventProducer: (Summary.Event) -> Unit
	): Summary.State? {
		eventProducer(Summary.Event.ShowProfilePictureUpdatedSnackBar)

		return if (currentState is Summary.State.Loaded) {
			val newState = currentState.value.copy(
				profilePictureUrl = useCaseState.value,
				isProfilePictureLoading = false
			)

			Summary.State.Loaded(newState)
		} else {
			super.reduceDataState(currentState, useCaseState, eventProducer)
		}
	}

	override suspend fun reduceErrorState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Error<String, ProfilePictureError>,
		eventProducer: (Summary.Event) -> Unit
	): Summary.State? {
		when (val error = useCaseState.error) {
			is ProfilePictureError.Timeout ->
				eventProducer(Summary.Event.ShowTimeoutSnackBar)

			is ProfilePictureError.NoConnection ->
				eventProducer(Summary.Event.ShowNoConnectionSnackBar(error.isNetworkAvailable))

			else ->
				eventProducer(Summary.Event.ShowDefaultErrorSnackBar)
		}

		return if (currentState is Summary.State.Loaded) {
			val newState = currentState.value.copy(
				isProfilePictureLoading = false
			)

			Summary.State.Loaded(newState)
		} else {
			super.reduceErrorState(currentState, useCaseState, eventProducer)
		}
	}
}