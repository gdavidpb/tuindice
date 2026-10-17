package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantComponentSheet
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantComponentSheetInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantComponentSheetMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

// The sheet of a component. Open starts following the store; the first value decides whether the sheet has content or
// closes. The store's later values only update the content, or close the sheet when the component is gone.
internal fun MachineDefinitionBuilder<AssistantComponentSheet.State>.assistantComponentSheetTransitions(
	machine: AssistantComponentSheetMachine,
	host: MachineHost<AssistantComponentSheet.Effect>
) {
	from<AssistantComponentSheet.State.Idle> {
		on<AssistantComponentSheet.Action.Open> { state, action ->
			machine.observe(host = host, messageId = action.messageId, componentId = action.componentId)

			state
		}

		onTo<
			AssistantComponentSheetInternalEvent.ComponentObserved,
			AssistantComponentSheet.State.Content
		> { _, event ->
			AssistantComponentSheet.State.Content(messageId = event.messageId, component = event.component)
		}

		onTo<AssistantComponentSheetInternalEvent.ComponentMissing, AssistantComponentSheet.State.Missing>(
			emits = setOf(AssistantComponentSheet.Effect.Dismiss::class)
		) { _, _ ->
			host.sendEffect(AssistantComponentSheet.Effect.Dismiss)

			AssistantComponentSheet.State.Missing
		}
	}

	from<AssistantComponentSheet.State.Content> {
		on<AssistantComponentSheetInternalEvent.ComponentObserved> { state, event ->
			state.copy(component = event.component)
		}

		onTo<AssistantComponentSheetInternalEvent.ComponentMissing, AssistantComponentSheet.State.Missing>(
			emits = setOf(AssistantComponentSheet.Effect.Dismiss::class)
		) { _, _ ->
			host.sendEffect(AssistantComponentSheet.Effect.Dismiss)

			AssistantComponentSheet.State.Missing
		}

		// A link inside the component leaves for its target and closes the sheet, as a link in the chat does.
		on<AssistantComponentSheet.Action.OpenNavTarget>(
			emits = setOf(
				AssistantComponentSheet.Effect.NavigateTo::class,
				AssistantComponentSheet.Effect.Dismiss::class
			)
		) { state, action ->
			host.sendEffect(
				AssistantComponentSheet.Effect.NavigateTo(target = action.target, entityRefs = action.entityRefs)
			)
			host.sendEffect(AssistantComponentSheet.Effect.Dismiss)

			state
		}
	}
}
