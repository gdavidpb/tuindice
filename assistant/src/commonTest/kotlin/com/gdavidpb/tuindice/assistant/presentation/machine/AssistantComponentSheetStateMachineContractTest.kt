package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantComponentSheet
import com.gdavidpb.tuindice.assistant.presentation.mapper.toChatComponentItem
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantComponentSheetFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.chatcontract.component.ComponentDisplay
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.UnknownChatComponent
import com.gdavidpb.tuindice.chatcontract.message.Component
import com.gdavidpb.tuindice.testkit.mvi.RecordingMachineHost
import com.gdavidpb.tuindice.testkit.mvi.UnreachableRow
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineHasNoShadowedRows
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

// Static table contract and a seeded random walk over the rows of the component sheet, on the host.
class AssistantComponentSheetStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() = runTest {
		val definition = AssistantComponentSheetFixture().machine.define(RecordingMachineHost(backgroundScope))

		assertMachineCoversAlphabet(
			definition,
			AssistantComponentSheet.Action::class,
			AssistantComponentSheetInternalEvent::class
		)

		assertMachineHasNoShadowedRows(
			definition,
			AssistantComponentSheet.Action::class,
			AssistantComponentSheetInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = definition,
			initialState = AssistantComponentSheet.State.Idle::class
		)

		assertMachineCoversEffects(definition, AssistantComponentSheet.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() = runTest {
		val diagram = AssistantComponentSheetFixture().machine
			.define(RecordingMachineHost(backgroundScope))
			.exportToMermaid(
				machineName = "assistant_component_sheet",
				initialState = AssistantComponentSheet.State.Idle::class
			)

		val expectedFragments = listOf("idle", "content", "Open", "ComponentObserved", "ComponentMissing")

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	// The sheet closes on a missing component and has no way out, so a walk that draws that event ends there. The
	// coverage floor is measured without it, over a store that holds the component; the walk over the store is what the
	// rows read. The rows that event closes the sheet by are unreachable here, and named as such.
	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		assertMachineRandomWalk(
			screenMachine = AssistantComponentSheetFixture(initial = conversationWithComponent()).machine,
			sampleEvents = sampleEvents().filterNot { event ->
				event == AssistantComponentSheetInternalEvent.ComponentMissing
			},
			coroutineScope = backgroundScope,
			unreachableRows = listOf(
				UnreachableRow(
					from = AssistantComponentSheet.State.Idle::class,
					on = AssistantComponentSheetInternalEvent.ComponentMissing::class,
					reason = "the store holds the component, so the walk never draws the event that closes the sheet"
				),
				UnreachableRow(
					from = AssistantComponentSheet.State.Content::class,
					on = AssistantComponentSheetInternalEvent.ComponentMissing::class,
					reason = "the store holds the component, so the walk never draws the event that closes the sheet"
				),
				UnreachableRow(
					from = AssistantComponentSheet.State.Idle::class,
					on = AssistantComponentSheet.Action.Open::class,
					reason = "the route sends Open once, before any step; the walk leaves Idle at the first observed " +
						"component, so Idle/Open is not a row the walk can keep reaching"
				)
			),
			// The reachable rows are Idle/ComponentObserved and the three Content rows (observed, unhandled, link).
			minRowCoverage = 0.6
		)
	}

	// Every event is walked once more, with no floor, so the rows that close the sheet keep their contracts too.
	@Test
	fun machine_keepsTheRowContracts_whenEveryEventCanCloseTheSheet() = runTest {
		assertMachineRandomWalk(
			screenMachine = AssistantComponentSheetFixture(initial = conversationWithComponent()).machine,
			sampleEvents = sampleEvents(),
			coroutineScope = backgroundScope,
			seed = 0x51DE,
			minRowCoverage = 0.0
		)
	}

	private fun conversationWithComponent() = assistantConversation(
		items = listOf(agentMessage("message-1", Component(component = component())))
	)

	private fun component() = UnknownChatComponent(
		id = "component-1",
		fallbackText = "Resumen",
		display = ComponentDisplay.SHEET
	)

	private fun sampleEvents(): List<Any> {
		return listOf(
			AssistantComponentSheet.Action.Open(messageId = "message-1", componentId = "component-1"),
			AssistantComponentSheet.Action.OpenNavTarget(target = NavTarget.SUMMARY, entityRefs = emptyList()),
			AssistantComponentSheet.Action.Unhandled(AssistantChat.Action.Send),
			AssistantComponentSheetInternalEvent.ComponentObserved(
                messageId = "message-1",
                item = component().toChatComponentItem(nowMillis = 0L)
            ),
			AssistantComponentSheetInternalEvent.ComponentMissing
		)
	}
}
