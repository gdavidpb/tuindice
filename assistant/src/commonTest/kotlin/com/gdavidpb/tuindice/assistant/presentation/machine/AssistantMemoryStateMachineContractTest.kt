package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.model.MemoryEntryItem
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantMemoryFixture
import com.gdavidpb.tuindice.testkit.mvi.RecordingMachineHost
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineHasNoShadowedRows
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

// Static table contract (alphabet, reachability, Λ coverage, Mermaid) and a seeded random walk over the rows, on the
// host. The rows read the texts through a fake, so every path runs without the compose resources.
class AssistantMemoryStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() = runTest {
		val definition = AssistantMemoryFixture().machine.define(RecordingMachineHost(backgroundScope))

		assertMachineCoversAlphabet(
			definition,
			AssistantMemory.Action::class,
			AssistantMemoryInternalEvent::class
		)

		assertMachineHasNoShadowedRows(
			definition,
			AssistantMemory.Action::class,
			AssistantMemoryInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = definition,
			initialState = AssistantMemory.State.Idle::class
		)

		assertMachineCoversEffects(definition, AssistantMemory.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() = runTest {
		val diagram = AssistantMemoryFixture().machine
			.define(RecordingMachineHost(backgroundScope))
			.exportToMermaid(
				machineName = "assistant_memory",
				initialState = AssistantMemory.State.Idle::class
			)

		val expectedFragments = listOf(
			"idle",
			"content",
			"Load",
			"EntriesLoaded",
			"DeleteEntry",
			"LastEntryDeleted",
			"ConfirmClearAll"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		assertMachineRandomWalk(
			screenMachine = AssistantMemoryFixture().machine,
			sampleEvents = sampleEvents(),
			coroutineScope = backgroundScope,
			minRowCoverage = 0.6
		)
	}

	private fun sampleEvents(): List<Any> = sampleActions() + sampleInternalEvents()

	private fun sampleActions(): List<Any> {
		return listOf(
			AssistantMemory.Action.Load,
			AssistantMemory.Action.Retry,
			AssistantMemory.Action.DeleteEntry(memoryId = "m1"),
			AssistantMemory.Action.AnswerStillApplies(memoryId = "m1", applies = true),
			AssistantMemory.Action.ClearAll,
			AssistantMemory.Action.ConfirmClearAll
		)
	}

	private fun sampleInternalEvents(): List<Any> {
		val entry = MemoryEntryItem(
			id = "m1",
			text = "Prefiero clases por la mañana",
			categoryLabel = "label:PREFERENCE",
			anchorLabel = null,
			needsConfirmation = false
		)

		return listOf(
			AssistantMemoryInternalEvent.EntriesLoaded(entries = listOf(entry)),
			AssistantMemoryInternalEvent.NothingLoaded,
			AssistantMemoryInternalEvent.LoadFailed(message = "load-failed"),
			AssistantMemoryInternalEvent.EntryDeleted(memoryId = "m1", message = "deleted"),
			AssistantMemoryInternalEvent.LastEntryDeleted(memoryId = "m1", message = "deleted"),
			AssistantMemoryInternalEvent.EntryDeleteFailed(message = "delete-failed"),
			AssistantMemoryInternalEvent.StillAppliesAnswered,
			AssistantMemoryInternalEvent.StillAppliesFailed(message = "answer-failed"),
			AssistantMemoryInternalEvent.MemoryCleared(message = "cleared"),
			AssistantMemoryInternalEvent.ClearFailed(message = "delete-failed")
		)
	}
}
