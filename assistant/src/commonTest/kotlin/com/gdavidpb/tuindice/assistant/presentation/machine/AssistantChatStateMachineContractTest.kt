package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.usecase.error.ExtractEvaluationsFromPhotoError
import com.gdavidpb.tuindice.assistant.domain.usecase.error.SendAssistantMessageError
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantChatFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.emptyAssistantConversation
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.feedback.ReportReason
import com.gdavidpb.tuindice.testkit.mvi.RecordingMachineHost
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineHasNoShadowedRows
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

// Static table contract (alphabet, reachability, Λ coverage, Mermaid) and a seeded random walk over the rows, on the
// host. The rows read the texts through a fake, so every path runs without the compose resources.
class AssistantChatStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() = runTest {
		val definition = AssistantChatFixture().machine.define(RecordingMachineHost(backgroundScope))

		assertMachineCoversAlphabet(
			definition,
			AssistantChat.Action::class,
			AssistantChatInternalEvent::class
		)

		assertMachineHasNoShadowedRows(
			definition,
			AssistantChat.Action::class,
			AssistantChatInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = definition,
			initialState = AssistantChat.State.Idle::class
		)

		assertMachineCoversEffects(definition, AssistantChat.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() = runTest {
		val diagram = AssistantChatFixture().machine
			.define(RecordingMachineHost(backgroundScope))
			.exportToMermaid(
				machineName = "assistant_chat",
				initialState = AssistantChat.State.Idle::class
			)

		val expectedFragments = listOf(
			"idle",
			"content",
			"Open",
			"OpeningReady",
			"ConversationObserved",
			"SendRejected",
			"OpenNavTarget / NavigateTo"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	// Unavailable has no way out, so a walk that draws the events closing the sheet ends there: the coverage floor is
	// measured without them, and every event is walked once more, with no floor, for the row contracts.
	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		assertMachineRandomWalk(
			screenMachine = AssistantChatFixture().machine,
			sampleEvents = sampleEvents().filterNot { event -> event in closingEvents },
			coroutineScope = backgroundScope,
			minRowCoverage = 0.6
		)
	}

	@Test
	fun machine_keepsTheRowContracts_whenEveryEventCanCloseTheSheet() = runTest {
		assertMachineRandomWalk(
			screenMachine = AssistantChatFixture().machine,
			sampleEvents = sampleEvents(),
			coroutineScope = backgroundScope,
			seed = 0x51DE,
			minRowCoverage = 0.0
		)
	}

	private val closingEvents: List<Any> = listOf(
		AssistantChatInternalEvent.OpeningUnavailable,
		AssistantChatInternalEvent.AssistantOff,
		AssistantChatInternalEvent.SendDisabled
	)

	private fun sampleEvents(): List<Any> = sampleActions() + sampleInternalEvents()

	private fun sampleActions(): List<Any> {
		return listOf(
			AssistantChat.Action.Open(context = null),
			AssistantChat.Action.Open(
				context = AssistantScreenContext(
					screen = AssistantScreen.EVALUATIONS,
					visibleFields = mapOf("subject" to "CI4325")
				)
			),
			AssistantChat.Action.ChangeText(text = "hola"),
			AssistantChat.Action.Send,
			AssistantChat.Action.PickSuggestion(suggestionId = "s1"),
			AssistantChat.Action.RemoveContext,
			AssistantChat.Action.RetryTurn,
			AssistantChat.Action.ConfirmIntent(intentId = "i1"),
			AssistantChat.Action.DismissIntent(intentId = "i1"),
			AssistantChat.Action.ToggleIntentDraft(intentId = "i1", mutationId = "m1", selected = false),
			AssistantChat.Action.CopyMessage(messageId = "a1"),
			AssistantChat.Action.RateMessage(messageId = "a1", useful = true),
			AssistantChat.Action.ReportMessage(messageId = "a1", reason = ReportReason.OTHER, comment = null),
			AssistantChat.Action.OpenNavTarget(target = NavTarget.SUMMARY, entityRefs = emptyList()),
			AssistantChat.Action.OpenComponentSheet(messageId = "a1", componentId = "c1"),
			AssistantChat.Action.AnswerStillApplies(memoryId = "mem1", applies = true),
			AssistantChat.Action.Appeal(text = "revisen"),
			AssistantChat.Action.AcceptConsent,
			AssistantChat.Action.DeclineConsent,
			AssistantChat.Action.Dismiss,
			AssistantChat.Action.RequestPhoto(fromCamera = true),
			AssistantChat.Action.RequestPhoto(fromCamera = false),
			AssistantChat.Action.AttachPhoto(file = PlatformFile("assistant-plan.jpg"))
		)
	}

	private fun sampleInternalEvents(): List<Any> {
		val failure = SendAssistantMessageError.QuotaExhausted(retryAfterSeconds = 30)

		return listOf(
			AssistantChatInternalEvent.OpeningUnavailable,
			AssistantChatInternalEvent.OpeningNeedsConsent,
			AssistantChatInternalEvent.OpeningReady,
			AssistantChatInternalEvent.AssistantOff,
			AssistantChatInternalEvent.AssistantOn(guided = true),
			AssistantChatInternalEvent.ConsentAccepted,
			AssistantChatInternalEvent.ConversationObserved(conversation = emptyAssistantConversation()),
			AssistantChatInternalEvent.TurnAccepted(turnId = "t1", lastSeq = 0),
			AssistantChatInternalEvent.SendDisabled,
			AssistantChatInternalEvent.SendConsentRequired,
			AssistantChatInternalEvent.SendRejected(
				error = failure,
				sentText = "hola",
				restoreText = "hola",
				suggestionId = null,
				clientTurnId = "client-1"
			),
			AssistantChatInternalEvent.SendRejected(
				error = SendAssistantMessageError.Other,
				sentText = "hola",
				restoreText = null,
				suggestionId = "s1",
				clientTurnId = "client-2"
			),
			AssistantChatInternalEvent.TurnRunFailed(code = RunErrorCode.TRANSIENT, retryable = true),
			AssistantChatInternalEvent.RestrictionChecked(standing = null),
			AssistantChatInternalEvent.IntentConfirmFinished(intentId = "i1"),
			AssistantChatInternalEvent.EnrollmentProofDelegated(intentId = "i1"),
			AssistantChatInternalEvent.MessageRated(message = "rated"),
			AssistantChatInternalEvent.MessageReported(message = "reported"),
			AssistantChatInternalEvent.MessageReportRejected(message = "report-too-long"),
			AssistantChatInternalEvent.AppealSent(message = "appeal-sent"),
			AssistantChatInternalEvent.AppealRejected(message = "appeal-failed"),
			AssistantChatInternalEvent.PhotoExtracted,
			AssistantChatInternalEvent.PhotoRejected(error = ExtractEvaluationsFromPhotoError.QuotaExhausted(30)),
			AssistantChatInternalEvent.PhotoRejected(error = null)
		)
	}
}
