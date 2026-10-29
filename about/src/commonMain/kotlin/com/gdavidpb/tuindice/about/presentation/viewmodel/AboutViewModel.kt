package com.gdavidpb.tuindice.about.presentation.viewmodel

import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.machine.AboutMachine
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel

class AboutViewModel(
	override val screenMachine: AboutMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<About.State, About.Action, About.Effect>(
	name = "about",
	initialState = screenMachine.initialState(),
	initialAction = About.Action.LoadVersion,
	dispatchers = dispatchers
) {
	// The screen sends its actions through this single entry point, so the view model keeps one function
	// for every action instead of one wrapper per action.
	fun dispatchAction(action: About.Action) =
		sendAction(action)
}
