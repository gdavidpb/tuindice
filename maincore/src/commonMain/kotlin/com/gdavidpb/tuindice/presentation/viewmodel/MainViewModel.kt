package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.UpdateLaunchResult
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.machine.MainMachine

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

	fun setLastSectionAction(section: MainSection) =
		sendAction(Main.Action.SetLastMainSection(section = section))

	fun checkUpdateAction() =
		sendAction(Main.Action.RequestUpdateCheck)

	fun updateAppAction() =
		sendAction(Main.Action.ClickUpdateApp)

	fun updateFlowCompletedAction(result: UpdateLaunchResult) =
		sendAction(Main.Action.UpdateFlowCompleted(result = result))

	fun requestSyncAction() =
		sendAction(Main.Action.RequestSync)

	fun noteSyncUnavailableAction() =
		sendAction(Main.Action.NoteSyncUnavailable)

	fun noteSyncFailedAction() =
		sendAction(Main.Action.NoteSyncFailed)
}
