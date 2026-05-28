package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.summary.presentation.action.ConfirmRemoveProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.OpenProfilePictureSettingsActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.PickProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.RefreshSummaryActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.RemoveProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.ObserveSummaryActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.TakeProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.UploadProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.Flow

class SummaryViewModel(
	private val observeSummaryActionProcessor: ObserveSummaryActionProcessor,
	private val refreshSummaryActionProcessor: RefreshSummaryActionProcessor,
	private val takeProfilePictureActionProcessor: TakeProfilePictureActionProcessor,
	private val pickProfilePictureActionProcessor: PickProfilePictureActionProcessor,
	private val uploadProfilePictureActionProcessor: UploadProfilePictureActionProcessor,
	private val confirmRemoveProfilePictureActionProcessor: ConfirmRemoveProfilePictureActionProcessor,
	private val removeProfilePictureActionProcessor: RemoveProfilePictureActionProcessor,
	private val openProfilePictureSettingsActionProcessor: OpenProfilePictureSettingsActionProcessor
) : BaseViewModel<Summary.State, Summary.Action, Summary.Effect>(
	initialState = Summary.State.Idle,
	initialAction = Summary.Action.ObserveSummary
) {

	fun refreshSummaryAction() =
		sendAction(Summary.Action.RefreshSummary)

	fun takeProfilePictureAction() =
		sendAction(Summary.Action.TakeProfilePicture)

	fun pickProfilePictureAction() =
		sendAction(Summary.Action.PickProfilePicture)

	fun uploadProfilePictureAction(file: PlatformFile) =
		sendAction(Summary.Action.UploadProfilePicture(file))

	fun removeProfilePictureAction() =
		sendAction(Summary.Action.RemoveProfilePicture)

	fun confirmRemoveProfilePictureAction() =
		sendAction(Summary.Action.ConfirmRemoveProfilePicture)

	fun openProfilePictureSettingsAction() =
		sendAction(Summary.Action.OpenProfilePictureSettings)

	override suspend fun processAction(
		action: Summary.Action,
		sideEffect: (Summary.Effect) -> Unit
	): Flow<Mutation<Summary.State>> {
		return when (action) {
			is Summary.Action.ObserveSummary ->
				observeSummaryActionProcessor.process(action, sideEffect)

			is Summary.Action.RefreshSummary ->
				refreshSummaryActionProcessor.process(action, sideEffect)

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
		}
	}
}
