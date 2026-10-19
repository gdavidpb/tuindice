package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.usecase.ObserveAssistantConversationUseCase
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantComponentSheet
import com.gdavidpb.tuindice.assistant.presentation.transition.assistantComponentSheetTransitions
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.assistant.presentation.mapper.toChatComponentItem
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.message.Component
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

/**
 * The sheet of one component of the conversation. It reads the conversation store and follows it while it is open; it
 * closes when the component leaves the store. It keeps nothing of its own: the store is the source of the component.
 */
class AssistantComponentSheetMachine(
	private val observeAssistantConversationUseCase: ObserveAssistantConversationUseCase
) : ScreenMachine<AssistantComponentSheet.State, AssistantComponentSheet.Effect> {
	override fun initialState(): AssistantComponentSheet.State = AssistantComponentSheet.State.Idle

	override fun define(
		host: MachineHost<AssistantComponentSheet.Effect>
	): MachineDefinition<AssistantComponentSheet.State> {
		return MachineDefinition.define {
			assistantComponentSheetTransitions(machine = this@AssistantComponentSheetMachine, host = host)
		}
	}

	// The store is observed from its current value on. Each value is looked up for the component; the lookup is told
	// once per change, so a component that is gone is told as missing once.
	internal fun observe(host: MachineHost<AssistantComponentSheet.Effect>, messageId: String, componentId: String) {
		host.launchMachineJob {
			observeAssistantConversationUseCase.execute(Unit)
				.mapNotNull { useCaseState -> (useCaseState as? UseCaseState.Data)?.value }
				.map { conversation -> conversation.componentOf(messageId = messageId, componentId = componentId) }
				.distinctUntilChanged()
				.collect { component ->
					host.processInternalEvent(
						if (component == null) {
							AssistantComponentSheetInternalEvent.ComponentMissing
						} else {
							AssistantComponentSheetInternalEvent.ComponentObserved(
								messageId = messageId,
								item = component.toChatComponentItem()
							)
						}
					)
				}
		}
	}
}

// The component [componentId] of the message [messageId], as the conversation holds it; null when it is not there.
private fun AssistantConversation.componentOf(messageId: String, componentId: String): ChatComponent? {
	val message = items
		.filterIsInstance<AssistantConversationItem.Agent>()
		.map { item -> item.message }
		.firstOrNull { message -> message.id == messageId }

	return message?.parts
		?.filterIsInstance<Component>()
		?.firstOrNull { part -> part.component.id == componentId }
		?.component
}
