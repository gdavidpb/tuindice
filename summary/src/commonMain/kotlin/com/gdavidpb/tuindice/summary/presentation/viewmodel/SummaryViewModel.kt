package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import io.github.vinceglb.filekit.PlatformFile

class SummaryViewModel(
	override val screenMachine: SummaryMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<Summary.State, Summary.Action, Summary.Effect>(
	name = "summary",
	initialState = screenMachine.initialState(),
	initialAction = Summary.Action.ObserveSummary,
	dispatchers = dispatchers
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
}
