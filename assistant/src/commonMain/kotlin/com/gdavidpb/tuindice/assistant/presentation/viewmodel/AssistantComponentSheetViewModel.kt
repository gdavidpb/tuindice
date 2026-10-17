package com.gdavidpb.tuindice.assistant.presentation.viewmodel

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantComponentSheet
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantComponentSheetMachine
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel

// The component sheet's API. The route opens it with Open, which names the component to show.
class AssistantComponentSheetViewModel(
	override val screenMachine: AssistantComponentSheetMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<
	AssistantComponentSheet.State,
	AssistantComponentSheet.Action,
	AssistantComponentSheet.Effect
>(
	name = "assistant_component_sheet",
	initialState = screenMachine.initialState(),
	dispatchers = dispatchers
) {
	fun dispatch(action: AssistantComponentSheet.Action) {
		sendAction(action)
	}
}
