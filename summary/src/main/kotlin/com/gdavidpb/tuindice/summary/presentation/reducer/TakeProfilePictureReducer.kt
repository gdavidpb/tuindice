package com.gdavidpb.tuindice.summary.presentation.reducer

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.reducer.BaseReducer
import com.gdavidpb.tuindice.summary.domain.usecase.TakeProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary

class TakeProfilePictureReducer(
	override val useCase: TakeProfilePictureUseCase
) : BaseReducer<Unit, String, ProfilePictureError, Summary.State, Summary.Action.TakeProfilePicture, Summary.Event>() {
	override fun actionToParams(action: Summary.Action.TakeProfilePicture) {}

	override suspend fun reduceDataState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Data<String, ProfilePictureError>,
		eventProducer: (Summary.Event) -> Unit
	): Summary.State? {
		eventProducer(Summary.Event.OpenCamera(output = useCaseState.value))

		return super.reduceDataState(currentState, useCaseState, eventProducer)
	}

	override suspend fun reduceErrorState(
		currentState: Summary.State,
		useCaseState: UseCaseState.Error<String, ProfilePictureError>,
		eventProducer: (Summary.Event) -> Unit
	): Summary.State? {
		eventProducer(Summary.Event.ShowDefaultErrorSnackBar)

		return super.reduceErrorState(currentState, useCaseState, eventProducer)
	}
}