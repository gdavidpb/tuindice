package com.gdavidpb.tuindice.wizard.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.wizard.presentation.contract.CoachmarkOverlay
import com.gdavidpb.tuindice.wizard.presentation.machine.CoachmarkOverlayMachine
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkSurface

class CoachmarkOverlayViewModel(
	override val screenMachine: CoachmarkOverlayMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<CoachmarkOverlay.State, CoachmarkOverlay.Action, CoachmarkOverlay.Effect>(
	name = "coachmark-overlay",
	initialState = screenMachine.initialState(),
	dispatchers = dispatchers
) {
	fun surfaceChangedAction(surface: CoachmarkSurface) =
		sendAction(CoachmarkOverlay.Action.SurfaceChanged(surface = surface))

	fun primaryActionClickAction() =
		sendAction(CoachmarkOverlay.Action.PrimaryActionClick)

	fun previousActionClickAction() =
		sendAction(CoachmarkOverlay.Action.PreviousActionClick)
}
