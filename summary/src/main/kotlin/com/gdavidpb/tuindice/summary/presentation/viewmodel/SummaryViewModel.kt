package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.reducer.RemoveProfilePictureReducer
import com.gdavidpb.tuindice.summary.presentation.reducer.SummaryReducer
import com.gdavidpb.tuindice.summary.presentation.reducer.TakeProfilePictureReducer
import com.gdavidpb.tuindice.summary.presentation.reducer.UploadProfilePictureReducer

class SummaryViewModel(
	private val summaryReducer: SummaryReducer,
	private val takeProfilePictureReducer: TakeProfilePictureReducer,
	private val uploadProfilePictureReducer: UploadProfilePictureReducer,
	private val removeProfilePictureReducer: RemoveProfilePictureReducer
) : BaseViewModel<Summary.State, Summary.Action, Summary.Event>(initialViewState = Summary.State.Loading) {

	private var cameraOutput: String = ""

	fun loadSummaryAction() =
		emitAction(Summary.Action.LoadSummary)

	fun takeProfilePictureAction() =
		emitAction(Summary.Action.TakeProfilePicture)

	fun pickProfilePictureAction() =
		emitAction(Summary.Action.PickProfilePicture)

	fun uploadProfilePictureAction(path: String) =
		emitAction(Summary.Action.UploadProfilePicture(path))

	fun uploadTakenProfilePictureAction() =
		emitAction(Summary.Action.UploadProfilePicture(path = cameraOutput))
			.also { cameraOutput = "" }

	fun removeProfilePictureAction() =
		emitAction(Summary.Action.RemoveProfilePicture)

	fun showTryLaterAction() =
		emitAction(Summary.Action.ShowTryLater)

	override suspend fun reducer(action: Summary.Action) {
		when (action) {
			is Summary.Action.LoadSummary ->
				summaryReducer.reduce(
					action = action,
					currentState = { currentState },
					stateProducer = ::setState,
					eventProducer = ::sendEvent
				)

			is Summary.Action.TakeProfilePicture ->
				takeProfilePictureReducer.reduce(
					action = action,
					currentState = { currentState },
					stateProducer = ::setState,
					eventProducer = { event ->
						if (event is Summary.Event.OpenCamera) cameraOutput = event.output

						sendEvent(event)
					}
				)

			is Summary.Action.PickProfilePicture ->
				sendEvent(Summary.Event.OpenPicker)

			is Summary.Action.UploadProfilePicture ->
				uploadProfilePictureReducer.reduce(
					action = action,
					currentState = { currentState },
					stateProducer = ::setState,
					eventProducer = ::sendEvent
				)

			is Summary.Action.RemoveProfilePicture ->
				removeProfilePictureReducer.reduce(
					action = action,
					currentState = { currentState },
					stateProducer = ::setState,
					eventProducer = ::sendEvent
				)

			is Summary.Action.ShowTryLater ->
				sendEvent(Summary.Event.ShowTryLaterSnackBar)
		}
	}
}