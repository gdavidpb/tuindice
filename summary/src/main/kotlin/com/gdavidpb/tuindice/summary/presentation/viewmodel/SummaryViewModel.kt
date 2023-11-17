package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.summary.presentation.action.CloseDialogActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.ConfirmRemoveProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.LoadSummaryActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.OpenProfilePictureSettingsActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.PickProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.RemoveProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.TakeProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.UploadProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import kotlinx.coroutines.flow.Flow

class SummaryViewModel(
	private val loadSummaryActionProcessor: LoadSummaryActionProcessor,
	private val takeProfilePictureActionProcessor: TakeProfilePictureActionProcessor,
	private val pickProfilePictureActionProcessor: PickProfilePictureActionProcessor,
	private val uploadProfilePictureActionProcessor: UploadProfilePictureActionProcessor,
	private val confirmRemoveProfilePictureActionProcessor: ConfirmRemoveProfilePictureActionProcessor,
	private val removeProfilePictureActionProcessor: RemoveProfilePictureActionProcessor,
	private val closeDialogActionProcessor: CloseDialogActionProcessor,
	private val openProfilePictureSettingsActionProcessor: OpenProfilePictureSettingsActionProcessor
) : BaseViewModel<Summary.State, Summary.Action, Summary.Effect>(initialState = Summary.State.Loading) {

	private var cameraOutput: String = ""

	fun setCameraOutput(output: String) {
		cameraOutput = output
	}

	fun loadSummaryAction() =
		sendAction(Summary.Action.LoadSummary)

	fun takeProfilePictureAction() =
		sendAction(Summary.Action.TakeProfilePicture)

	fun pickProfilePictureAction() =
		sendAction(Summary.Action.PickProfilePicture)

	fun uploadProfilePictureAction(path: String) =
		sendAction(Summary.Action.UploadProfilePicture(path))

	fun uploadTakenProfilePictureAction() =
		sendAction(Summary.Action.UploadProfilePicture(path = cameraOutput))
			.also { cameraOutput = "" }

	fun removeProfilePictureAction() =
		sendAction(Summary.Action.RemoveProfilePicture)

	fun confirmRemoveProfilePictureAction() =
		sendAction(Summary.Action.ConfirmRemoveProfilePicture)

	fun openProfilePictureSettingsAction() =
		sendAction(Summary.Action.OpenProfilePictureSettings)

	fun closeDialogAction() =
		sendAction(Summary.Action.CloseDialog)

	override fun processAction(
		action: Summary.Action,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return when (action) {
			is Summary.Action.LoadSummary ->
				loadSummaryActionProcessor.process(action, sideEffect)

			is Summary.Action.TakeProfilePicture ->
				takeProfilePictureActionProcessor.process(action, sideEffect)

			is Summary.Action.PickProfilePicture ->
				pickProfilePictureActionProcessor.process(action, sideEffect)

			is Summary.Action.UploadProfilePicture ->
				uploadProfilePictureActionProcessor.process(action, sideEffect)

			is Summary.Action.ConfirmRemoveProfilePicture ->
				confirmRemoveProfilePictureActionProcessor.process(action, sideEffect)

			is Summary.Action.RemoveProfilePicture ->
				removeProfilePictureActionProcessor.process(action, sideEffect)

			is Summary.Action.OpenProfilePictureSettings ->
				openProfilePictureSettingsActionProcessor.process(action, sideEffect)

			is Summary.Action.CloseDialog ->
				closeDialogActionProcessor.process(action, sideEffect)
		}
	}
}