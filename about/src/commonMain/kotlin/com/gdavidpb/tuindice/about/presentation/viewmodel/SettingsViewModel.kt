package com.gdavidpb.tuindice.about.presentation.viewmodel

import com.gdavidpb.tuindice.about.presentation.contract.Settings
import com.gdavidpb.tuindice.about.presentation.machine.SettingsMachine
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel

class SettingsViewModel(
	override val screenMachine: SettingsMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<Settings.State, Settings.Action, Settings.Effect>(
	name = "settings",
	initialState = screenMachine.initialState(),
	initialAction = Settings.Action.LoadSettings,
	dispatchers = dispatchers
) {
	// The screen sends its actions through this single entry point, so the view model keeps one function
	// for every action instead of one wrapper per action.
	fun dispatchAction(action: Settings.Action) =
		sendAction(action)
}
