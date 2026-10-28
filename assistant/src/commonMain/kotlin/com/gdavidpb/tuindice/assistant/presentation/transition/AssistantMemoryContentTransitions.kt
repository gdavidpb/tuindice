package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantMemoryInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantMemoryMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost

private typealias MemoryContentRows = MachineDefinitionBuilder.StateTransitions<
	AssistantMemory.State,
	AssistantMemory.State.Content
>

// The list with rows. The student's actions are the rows of the first functions; the outcomes of the async commands are
// the rows of the last ones. Every row keeps its target: the list stays a list, becomes empty, or loads again.
internal fun MachineDefinitionBuilder<AssistantMemory.State>.assistantMemoryContentTransitions(
	machine: AssistantMemoryMachine,
	host: MachineHost<AssistantMemory.Effect>
) {
	from<AssistantMemory.State.Content> {
		deleteRows(machine, host)
		stillAppliesRows(machine, host)
		clearRows(machine, host)
	}
}

private fun MemoryContentRows.deleteRows(
	machine: AssistantMemoryMachine,
	host: MachineHost<AssistantMemory.Effect>
) {
	on<AssistantMemory.Action.DeleteEntry> { state, action ->
		// A delete on its way is not asked again: the entry stays listed until it ends, and a second request would find it
		// gone and tell the student it failed.
		if (action.memoryId in state.deleting || state.entries.none { entry -> entry.id == action.memoryId }) {
			return@on state
		}

		// The row decides whether the delete empties the list: the only row listed is the last one.
		machine.delete(
			host = host,
			memoryId = action.memoryId,
			isLast = state.entries.size == 1
		)
		state.copy(deleting = state.deleting + action.memoryId)
	}

	// The list is read when the delete ends: a delete that leaves it empty leaves the state empty, whichever delete was
	// asked first. Its target is the state the list becomes.
	onTo<AssistantMemoryInternalEvent.EntryDeleted, AssistantMemory.State>(
		emits = setOf(AssistantMemory.Effect.ShowSnackBar::class)
	) { state, event ->
		host.sendEffect(AssistantMemory.Effect.ShowSnackBar(message = event.message))

		val remaining = state.entries.filterNot { entry -> entry.id == event.memoryId }

		if (remaining.isEmpty()) {
			AssistantMemory.State.Empty
		} else {
			state.copy(entries = remaining, deleting = state.deleting - event.memoryId)
		}
	}

	onTo<AssistantMemoryInternalEvent.LastEntryDeleted, AssistantMemory.State.Empty>(
		emits = setOf(AssistantMemory.Effect.ShowSnackBar::class)
	) { _, event ->
		host.sendEffect(AssistantMemory.Effect.ShowSnackBar(message = event.message))

		AssistantMemory.State.Empty
	}

	on<AssistantMemoryInternalEvent.EntryDeleteFailed>(
		emits = setOf(AssistantMemory.Effect.ShowSnackBar::class)
	) { state, event ->
		host.sendEffect(AssistantMemory.Effect.ShowSnackBar(message = event.message))

		state.copy(deleting = state.deleting - event.memoryId)
	}
}

private fun MemoryContentRows.stillAppliesRows(
	machine: AssistantMemoryMachine,
	host: MachineHost<AssistantMemory.Effect>
) {
	on<AssistantMemory.Action.AnswerStillApplies> { state, action ->
		machine.answerStillApplies(host = host, memoryId = action.memoryId, applies = action.applies)

		state
	}

	// A saved answer loads the list again, as Load does.
	onTo<AssistantMemoryInternalEvent.StillAppliesAnswered, AssistantMemory.State.Loading> { _, _ ->
		machine.load(host = host)

		AssistantMemory.State.Loading
	}

	on<AssistantMemoryInternalEvent.StillAppliesFailed>(
		emits = setOf(AssistantMemory.Effect.ShowSnackBar::class)
	) { state, event ->
		host.sendEffect(AssistantMemory.Effect.ShowSnackBar(message = event.message))

		state
	}
}

private fun MemoryContentRows.clearRows(
	machine: AssistantMemoryMachine,
	host: MachineHost<AssistantMemory.Effect>
) {
	// The destructive action asks first; the dialog's confirmation comes back as ConfirmClearAll.
	on<AssistantMemory.Action.ClearAll>(
		emits = setOf(AssistantMemory.Effect.ShowClearAllConfirmation::class)
	) { state, _ ->
		host.sendEffect(AssistantMemory.Effect.ShowClearAllConfirmation)

		state
	}

	on<AssistantMemory.Action.ConfirmClearAll> { state, _ ->
		machine.clear(host = host)

		state
	}

	onTo<AssistantMemoryInternalEvent.MemoryCleared, AssistantMemory.State.Empty>(
		emits = setOf(AssistantMemory.Effect.ShowSnackBar::class)
	) { _, event ->
		host.sendEffect(AssistantMemory.Effect.ShowSnackBar(message = event.message))

		AssistantMemory.State.Empty
	}

	on<AssistantMemoryInternalEvent.ClearFailed>(
		emits = setOf(AssistantMemory.Effect.ShowSnackBar::class)
	) { state, event ->
		host.sendEffect(AssistantMemory.Effect.ShowSnackBar(message = event.message))

		state
	}
}
