package com.gdavidpb.tuindice.pensum.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.machine.PensumMachine

class PensumViewModel(
	override val screenMachine: PensumMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<Pensum.State, Pensum.Action, Pensum.Effect>(
	name = "pensum",
	initialState = screenMachine.initialState(),
	initialAction = Pensum.Action.ObservePensum,
	dispatchers = dispatchers
) {
	fun refreshPensumAction() {
		sendAction(Pensum.Action.RefreshPensum)
	}

	fun selectPensumAction(year: Int) {
		sendAction(Pensum.Action.SelectPensum(year = year))
	}

	fun selectModalityAction(modalityId: String) {
		sendAction(Pensum.Action.SelectModality(modalityId = modalityId))
	}

	fun selectSelectionAction(year: Int, modalityId: String) {
		sendAction(
			Pensum.Action.SelectSelection(
				year = year,
				modalityId = modalityId
			)
		)
	}

}
