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
			AssistantComponentSheet.State.Content(messageId = event.messageId, item = event.item)
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
			state.copy(item = event.item)
		}

		onTo<AssistantComponentSheetInternalEvent.ComponentMissing, AssistantComponentSheet.State.Missing>(
			emits = setOf(AssistantComponentSheet.Effect.Dismiss::class)
		) { _, _ ->
			host.sendEffect(AssistantComponentSheet.Effect.Dismiss)

			AssistantComponentSheet.State.Missing
		}

		// An action of the component other than a link is not the sheet's to take. The row takes it without a change or an
		// effect, so the sheet keeps its content and the message keeps the action.
		on<AssistantComponentSheet.Action.Unhandled> { state, _ -> state }

		// A link inside the component leaves for its target, as a link in the chat does: the host's jump closes this
		// sheet and the chat under it, so the sheet sends no Dismiss of its own, which would close what the jump opened.
		on<AssistantComponentSheet.Action.OpenNavTarget>(
			emits = setOf(AssistantComponentSheet.Effect.NavigateTo::class)
		) { state, action ->
			host.sendEffect(
				AssistantComponentSheet.Effect.NavigateTo(target = action.target, entityRefs = action.entityRefs)
			)

			state
		}
	}
}
