package com.gdavidpb.tuindice.assistant.presentation.transition

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatMachine
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatSession
import com.gdavidpb.tuindice.assistant.presentation.machine.nextMemoryRead
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.chatcontract.memory.MemoryVocabulary

/**
 * The answers to «¿Sigue aplicando?» and the memory states the service lists. A saved answer is kept; a refused one
 * waits for the read asked after its refusal, and the snackbar says so only when the memory still waits.
 */
internal fun MachineDefinitionBuilder.StateTransitions<
	AssistantChat.State,
	AssistantChat.State.Content
>.contentStillAppliesRows(
	machine: AssistantChatMachine,
	host: MachineHost<AssistantChat.Effect>
) {
	on<AssistantChatInternalEvent.StillAppliesSaved> { state, event ->
		val session = machine.session

		session.pendingMemories -= event.memoryId
		session.answeredMemories[event.memoryId] = AssistantChatSession.MemoryAnswer(
			applies = event.applies,
			readsBefore = session.memoryReadsRequested
		)
		machine.refreshed(state)
	}

	// The answer was refused: the answer waits for the read asked now, which says whether its memory still waits. The
	// buttons stay disabled until that read comes back.
	on<AssistantChatInternalEvent.StillAppliesFailed> { state, event ->
		val session = machine.session
		val read = session.nextMemoryRead()

		session.failedAnswers[event.memoryId] = read
		machine.feedback.loadMemoryStates(host, read = read)
		machine.refreshed(state)
	}

	// A read that comes back after a newer one does not overwrite it. A refused answer the read covers says so when its
	// memory still waits for it; one that was answered, archived or deleted meanwhile does not.
	on<AssistantChatInternalEvent.MemoryStatesLoaded>(emits = setOf(AssistantChat.Effect.ShowSnackBar::class)) {
		state, event ->
		val session = machine.session

		if (event.read > session.memoryStatesRead) {
			session.memoryStates = event.states
			session.memoryStatesRead = event.read
		}

		val covered = session.failedAnswers.filterValues { read -> read <= event.read }.keys

		if (covered.any { id -> event.states[id] == MemoryVocabulary.STATE_PENDING_CONFIRMATION }) {
			host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = machine.feedback.stillAppliesFailedText()))
		}

		session.failedAnswers -= covered
		session.pendingMemories -= covered
		machine.refreshed(state)
	}

	// A read that failed: the next observation asks again for the cards it did not cover. The refused answers it covers
	// say so, since the memory may still wait for them.
	on<AssistantChatInternalEvent.MemoryStatesFailed>(emits = setOf(AssistantChat.Effect.ShowSnackBar::class)) {
		state, event ->
		val session = machine.session

		if (event.read == session.memoryReadsRequested) session.memoryReadCards.clear()

		val covered = session.failedAnswers.filterValues { read -> read <= event.read }.keys

		if (covered.isNotEmpty()) {
			host.sendEffect(AssistantChat.Effect.ShowSnackBar(message = machine.feedback.stillAppliesFailedText()))
		}

		session.failedAnswers -= covered
		session.pendingMemories -= covered
		machine.refreshed(state)
	}
}
