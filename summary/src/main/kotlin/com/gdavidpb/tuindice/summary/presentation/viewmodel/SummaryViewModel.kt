package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.summary.domain.usecase.GetAccountUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.TakeProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.reducer.RemoveProfilePictureReducer
import com.gdavidpb.tuindice.summary.presentation.reducer.SummaryReducer
import com.gdavidpb.tuindice.summary.presentation.reducer.TakeProfilePictureReducer
import com.gdavidpb.tuindice.summary.presentation.reducer.UploadProfilePictureReducer

class SummaryViewModel(
	private val getAccountUseCase: GetAccountUseCase,
	private val takeProfilePictureUseCase: TakeProfilePictureUseCase,
	private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
	private val removeProfilePictureUseCase: RemoveProfilePictureUseCase,
	private val summaryReducer: SummaryReducer,
	private val takeProfilePictureReducer: TakeProfilePictureReducer,
	private val uploadProfilePictureReducer: UploadProfilePictureReducer,
	private val removeProfilePictureReducer: RemoveProfilePictureReducer,
) : BaseViewModel<Summary.State, Summary.Action, Summary.Event>(initialViewState = Summary.State.Loading) {

	private var cameraOutput: String = ""

	fun setCameraOutput(output: String) {
		cameraOutput = output
	}

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

	fun confirmRemoveProfilePictureAction() =
		emitAction(Summary.Action.ConfirmRemoveProfilePicture)

	fun openProfilePictureSettingsAction() =
		emitAction(Summary.Action.OpenProfilePictureSettings)

	fun closeDialogAction() =
		emitAction(Summary.Action.CloseDialog)

	override suspend fun reducer(action: Summary.Action) {
		when (action) {
			is Summary.Action.LoadSummary ->
				getAccountUseCase
					.execute(params = Unit)
					.collect(viewModel = this, reducer = summaryReducer)

			is Summary.Action.TakeProfilePicture ->
				takeProfilePictureUseCase
					.execute(params = Unit)
					.collect(viewModel = this, reducer = takeProfilePictureReducer)

			is Summary.Action.PickProfilePicture ->
				sendEvent(Summary.Event.OpenPicker)

			is Summary.Action.UploadProfilePicture ->
				uploadProfilePictureUseCase
					.execute(params = action.path)
					.collect(viewModel = this, reducer = uploadProfilePictureReducer)

			is Summary.Action.RemoveProfilePicture ->
				sendEvent(Summary.Event.ShowRemoveProfilePictureConfirmationDialog)

			is Summary.Action.ConfirmRemoveProfilePicture ->
				removeProfilePictureUseCase
					.execute(params = Unit)
					.collect(viewModel = this, reducer = removeProfilePictureReducer)

			is Summary.Action.CloseDialog ->
				sendEvent(Summary.Event.CloseDialog)

			is Summary.Action.OpenProfilePictureSettings -> {
				val currentState = getCurrentState()

				sendEvent(
					Summary.Event.ShowProfilePictureSettingsDialog(
						showRemove = currentState is Summary.State.Content && currentState.profilePictureUrl.isNotEmpty()
					)
				)
			}
		}
	}
}