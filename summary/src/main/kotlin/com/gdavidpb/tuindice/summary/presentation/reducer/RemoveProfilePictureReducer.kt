package com.gdavidpb.tuindice.summary.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary

class RemoveProfilePictureReducer(
	override val useCase: RemoveProfilePictureUseCase
) : BaseReducer<Unit, Unit, ProfilePictureError, Summary.State, Summary.Action.RemoveProfilePicture, Summary.Event>() {

	override fun actionToParams(action: Summary.Action.RemoveProfilePicture) {}

	override fun reduceUnrecoverableState(
		currentState: Summary.State,
		throwable: Throwable,
		eventProducer: (Summary.Event) -> Unit
	): Summary.State {
		return Summary.State.Failed
	}

	override suspend fun reduceLoadingState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Loading<Unit, ProfilePictureError>,
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
		useCaseState: UseCaseState.Data<Unit, ProfilePictureError>,
		eventProducer: (Summary.Event) -> Unit
	): Summary.State? {
		eventProducer(Summary.Event.ShowProfilePictureRemovedSnackBar)

		return if (currentState is Summary.State.Loaded) {
			val newState = currentState.value.copy(
				profilePictureUrl = "",
				isProfilePictureLoading = false
			)

			Summary.State.Loaded(newState)
		} else {
			super.reduceDataState(currentState, useCaseState, eventProducer)
		}
	}

	override suspend fun reduceErrorState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Error<Unit, ProfilePictureError>,
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