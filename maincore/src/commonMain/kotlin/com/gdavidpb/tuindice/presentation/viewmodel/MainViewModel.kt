package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.machine.MainMachine
import com.gdavidpb.tuindice.presentation.mapper.toMainSectionOrNull

class MainViewModel(
	override val screenMachine: MainMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<Main.State, Main.Action, Main.Effect>(
	name = "main",
	initialState = screenMachine.initialState(),
	initialAction = Main.Action.StartUp,
	dispatchers = dispatchers
) {
	fun requestReviewAction() =
		sendAction(Main.Action.RequestReview)

	fun startUpAction() =
		sendAction(Main.Action.StartUp)

	fun showOutdatedAppAction(state: OutdatedAppState) =
		sendAction(Main.Action.ShowOutdatedApp(outdatedAppState = state))

	fun setLastDestinationAction(destination: Destination) {
		destination.toMainSectionOrNull()
			?.let { sendAction(Main.Action.SetLastMainSection(section = it)) }
	}

	fun checkUpdateAction() =
		sendAction(Main.Action.RequestUpdateCheck)

	fun updateAppAction() =
		sendAction(Main.Action.ClickUpdateApp)

	fun requestSyncAction() =
		sendAction(Main.Action.RequestSync)

	fun requestWizardStartAction() =
		sendAction(Main.Action.RequestWizardStart)
}
