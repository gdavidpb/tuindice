package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.usecase.error.SendAssistantMessageError
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AssistantIntentOutcome
import com.gdavidpb.tuindice.assistant.presentation.model.AssistantReportReason
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantChatFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.NOW_MILLIS
import com.gdavidpb.tuindice.assistant.presentation.testing.acceptConsentAction
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.answerStillAppliesAction
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.changeTextAction
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.confirmIntentAction
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.dismissIntentAction
import com.gdavidpb.tuindice.assistant.presentation.testing.intentCard
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.testing.removeContextAction
import com.gdavidpb.tuindice.assistant.presentation.testing.reportMessageAction
import com.gdavidpb.tuindice.assistant.presentation.testing.retryTurnAction
import com.gdavidpb.tuindice.assistant.presentation.testing.sendMessageAction
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.assistant.testing.assistantIntent
import com.gdavidpb.tuindice.assistant.testing.assistantStatus
import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionResult
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.component.StillAppliesComponent
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.intent.RememberArgs
import com.gdavidpb.tuindice.chatcontract.memory.MemoryItem
import com.gdavidpb.tuindice.chatcontract.memory.MemoryList
import com.gdavidpb.tuindice.chatcontract.memory.MemoryVocabulary
import com.gdavidpb.tuindice.chatcontract.message.Component
import com.gdavidpb.tuindice.chatcontract.message.IntentPart
import com.gdavidpb.tuindice.chatcontract.message.Text
import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import com.gdavidpb.tuindice.testkit.mvi.RecordingMachineHost
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// What the outcomes of the jobs the content starts give the sheet when it is on the consent: the outcome is applied
// there, and the content shows it once the student comes back.
class AssistantChatLeavingContentBehaviorTest {
	// The table takes the acceptance of a send while the sheet asks for the consent: the sheet keeps the consent, and the
	// session holds the turn so its failure, and the reply, reach the content.
	@Test
	fun aTurnAccepted_whileTheSheetAsksForConsent_keepsTheSheetOnTheConsentAndHoldsTheTurn() = runTest {
		val fixture = AssistantChatFixture()
		val definition = fixture.machine.define(RecordingMachineHost(backgroundScope))

		val result = definition.process(
			AssistantChat.State.ConsentRequired,
			AssistantChatInternalEvent.TurnAccepted(turnId = "t1", lastSeq = 0, text = "hola", clientTurnId = "client-1")
		)

		assertEquals(AssistantChat.State.ConsentRequired, (result as TransitionResult.Transitioned).toState)
		assertEquals("t1", fixture.machine.session.sendingTurnId)
	}

	// The failure of the accepted turn, reported while the sheet asks for the consent, is the failure the content shows.
	@Test
	fun aRunErrorOfTheAcceptedTurn_whileTheSheetAsksForConsent_isItsFailure() = runTest {
		val fixture = AssistantChatFixture()
		val definition = fixture.machine.define(RecordingMachineHost(backgroundScope))
		definition.process(
			AssistantChat.State.ConsentRequired,
			AssistantChatInternalEvent.TurnAccepted(turnId = "t1", lastSeq = 0, text = "hola", clientTurnId = "client-1")
		)

		val result = definition.process(
			AssistantChat.State.ConsentRequired,
			AssistantChatInternalEvent.TurnRunFailed(turnId = "t1", code = RunErrorCode.TRANSIENT, retryable = true)
		)

		assertEquals(AssistantChat.State.ConsentRequired, (result as TransitionResult.Transitioned).toState)
		assertEquals("failure:TRANSIENT", fixture.machine.session.turnFailure?.message)
	}

	// A refusal of a send, reported while the sheet asks for the consent, keeps the quota the content shows.
	@Test
	fun aRefusedSend_whileTheSheetAsksForConsent_keepsItsQuota() = runTest {
		val fixture = AssistantChatFixture()
		val definition = fixture.machine.define(RecordingMachineHost(backgroundScope))

		val result = definition.process(
			AssistantChat.State.ConsentRequired,
			AssistantChatInternalEvent.SendRejected(
				error = SendAssistantMessageError.QuotaExhausted(retryAfterSeconds = 90),
				sentText = "hola",
				restoreText = null,
				suggestionId = null,
				clientTurnId = "client-1"
			)
		)

		assertEquals(AssistantChat.State.ConsentRequired, (result as TransitionResult.Transitioned).toState)
		assertEquals(90, fixture.machine.session.quota?.retryAfterSeconds)
	}

	// The same acceptance, on the consent the sheet falls back to when a consent is refused: the table applies it
	// there too.
	@Test
	fun aTurnAccepted_whileTheConsentIsRefused_isAppliedToo() = runTest {
		val fixture = AssistantChatFixture()
		val definition = fixture.machine.define(RecordingMachineHost(backgroundScope))

		val result = definition.process(
			AssistantChat.State.ConsentFailed,
			AssistantChatInternalEvent.TurnAccepted(turnId = "t1", lastSeq = 0, text = "hola", clientTurnId = "client-1")
		)

		assertEquals(AssistantChat.State.ConsentFailed, (result as TransitionResult.Transitioned).toState)
		assertEquals("t1", fixture.machine.session.sendingTurnId)
	}

	// The assistant turned off while a send was on its way: the send's outcome closes the sheet on the consent too.
	@Test
	fun aSendDisabled_whileTheSheetAsksForConsent_closesTheSheet() = runTest {
		val fixture = AssistantChatFixture()
		val definition = fixture.machine.define(RecordingMachineHost(backgroundScope))

		val result = definition.process(AssistantChat.State.ConsentRequired, AssistantChatInternalEvent.SendDisabled)

		assertEquals(AssistantChat.State.Unavailable, (result as TransitionResult.Transitioned).toState)
	}

	// A confirmation that fails while the sheet asks for the consent gives its card its buttons back, and says the send
	// failed, as it does on the content.
	@Test
	fun aConfirmationThatFails_whileTheSheetAsksForConsent_givesItsCardItsButtonsBack() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		val intent = rememberIntent(id = "i1")
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a1", IntentPart(intent))),
			openIntents = mapOf("i1" to intent)
		)
		fixture.intents.decideError = IllegalStateException("offline")
		fixture.gatedIntents.gated = true
		viewModel.openAction(null)
		confirmingWhileAPhotoAsksForConsent(fixture, viewModel, intentId = "i1")

		fixture.gatedIntents.release.complete(Unit)
		viewModel.acceptConsentAction()

		val content = viewModel.content()
		assertEquals(IntentCardStatus.Open, content.intentCard("i1").status)
		assertEquals(ChatNoticeItem.TurnFailed(message = "confirm-failed", canRetry = false), content.notice)
	}

	// A confirmation that ends while the sheet asks for the consent leaves no mark of a run behind it.
	@Test
	fun aConfirmationThatEnds_whileTheSheetAsksForConsent_leavesNoRunningMark() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		val intent = rememberIntent(id = "i1")
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a1", IntentPart(intent))),
			openIntents = mapOf("i1" to intent)
		)
		fixture.gatedIntents.gated = true
		viewModel.openAction(null)
		confirmingWhileAPhotoAsksForConsent(fixture, viewModel, intentId = "i1")

		fixture.gatedIntents.release.complete(Unit)

		assertFalse("i1" in fixture.machine.session.runningIntents)
	}

	// A confirmation still running when the consent is given keeps its mark: the consent does not end a run.
	@Test
	fun aConfirmationStillRunning_whenTheSheetAsksForConsent_staysRunningUntilItEnds() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		val intent = rememberIntent(id = "i1")
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a1", IntentPart(intent))),
			openIntents = mapOf("i1" to intent)
		)
		fixture.gatedIntents.gated = true
		viewModel.openAction(null)
		confirmingWhileAPhotoAsksForConsent(fixture, viewModel, intentId = "i1")

		viewModel.acceptConsentAction()
		assertEquals(IntentCardStatus.Running, viewModel.content().intentCard("i1").status)

		fixture.gatedIntents.release.complete(Unit)
		assertEquals(
			IntentCardStatus.Resolved(AssistantIntentOutcome.CONFIRMED),
			viewModel.content().intentCard("i1").status
		)
	}

	// A confirmation that has ended leaves no mark behind when the content is left; one that still runs keeps its mark.
	@Test
	fun leavingTheContent_dropsTheMarkOfAFinishedConfirmation_keepsARunningOne() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
		val session = fixture.machine.session
		session.runningIntents["finished"] = Job().apply { complete() }
		session.runningIntents["running"] = Job()

		fixture.machine.leaveContent()

		assertEquals(setOf("running"), session.runningIntents.keys)
	}

	// The retry of an unconfirmed turn is offered again when the content opens again, as its first observation offers it.
	// The turn was sent by this sheet, so the message it answers is the one it was sent with: a message refused meanwhile
	// took the retry while the sheet was on the content.
	@Test
	fun leavingTheContent_offersTheRetryAgain_whenTheContentOpensAgain() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		val sent = fixture.turns.requests.single()

		fixture.turns.sendError = AssistantRequestRefusedException("unknown", null, "unknown")
		viewModel.changeTextAction("adiós")
		viewModel.sendMessageAction()
		fixture.extraction.failure = AssistantRequestRefusedException(AssistantErrorCodes.CONSENT_REQUIRED, null, "consent")
		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))
		assertEquals(AssistantChat.State.ConsentRequired, viewModel.state.value)

		fixture.turns.sendError = null
		viewModel.acceptConsentAction()
		viewModel.retryTurnAction()

		val retried = fixture.turns.requests.last()
		assertEquals(sent.clientTurnId, retried.clientTurnId)
		assertEquals("hola", retried.message.text)
	}

	// A new message that waits for the recovery of the unconfirmed answer is not sent when the content is left. Its text
	// goes back to the composer, which shows it again when the content comes back, so the student keeps what they wrote.
	@Test
	fun aNewMessageWaitingForARecovery_givesItsTextBack_whenTheContentIsLeft() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.turns.acceptedTurn = TurnAccepted(turnId = "t0", conversationId = "c1", lastSeq = 0)
		viewModel.openAction(null)
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		// The recovery of t0 waits, and the new message waits for it with its text taken out of the composer.
		fixture.turns.holdEvents = true
		viewModel.changeTextAction("adiós")
		viewModel.sendMessageAction()
		assertEquals("", viewModel.content().composer.text)

		// The content is left while the recovery waits: the recovery stops, and the message is dropped with it.
		fixture.machine.leaveContent()

		assertEquals("adiós", fixture.machine.refreshed(viewModel.content()).composer.text)
		assertEquals(listOf("hola"), fixture.turns.requests.map { request -> request.message.text })
	}

	// A dismissal that fails while the sheet asks for the consent says so when the student comes back, as on the content.
	@Test
	fun aDismissalThatFails_onConsent_saysDismissFailedWhenTheStudentComesBack() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		val intent = rememberIntent(id = "i1")
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a1", IntentPart(intent))),
			openIntents = mapOf("i1" to intent)
		)
		fixture.intents.decideError = IllegalStateException("offline")
		fixture.gatedIntents.gated = true
		viewModel.openAction(null)
		viewModel.dismissIntentAction("i1")
		askConsentByPhoto(fixture, viewModel)

		fixture.gatedIntents.release.complete(Unit)
		viewModel.acceptConsentAction()

		assertEquals(ChatNoticeItem.TurnFailed(message = "dismiss-failed", canRetry = false), viewModel.content().notice)
	}

	// A restriction the standing gives while the sheet asks for the consent is the restriction the student finds once the
	// consent is given: the standing's end and its appeal, not the ones the refusal left. The end of the first restriction
	// passes, and the next send reads the standing again, which asks for the consent while it is read.
	@Test
	fun aRestrictionChecked_onConsent_isTheRestrictionTheStudentFinds() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
		fixture.standing.answer = restrictedUntil(NOW_MILLIS + 1_000L)
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.SUSPENDED, null, "suspended")
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()

		fixture.clock.nowMillis = NOW_MILLIS + 2_000L
		fixture.standing.answer = restrictedUntil(NOW_MILLIS + 10_000L)
		fixture.standing.gate = CompletableDeferred()
		fixture.turns.sendError = AssistantRequestRefusedException(AssistantErrorCodes.CONSENT_REQUIRED, null, "consent")
		viewModel.removeContextAction()
		viewModel.changeTextAction("hola")
		viewModel.sendMessageAction()
		assertEquals(AssistantChat.State.ConsentRequired, viewModel.state.value)

		fixture.standing.gate?.complete(Unit)
		viewModel.acceptConsentAction()

		val notice = viewModel.content().notice as? ChatNoticeItem.Restricted
		assertTrue(notice?.canAppeal == true)
	}

	// An answer about a memory saved while the sheet asks for the consent is the answer the card shows once the consent is
	// given: it is not waited for any more.
	@Test
	fun aStillAppliesAnswerSaved_onConsent_isTheAnswerTheCardShows() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		fixture.gatedMemories.gated = true
		viewModel.openAction(null)
		viewModel.answerStillAppliesAction("mem1", applies = true)
		askConsentByPhoto(fixture, viewModel)

		fixture.gatedMemories.release.complete(Unit)
		viewModel.acceptConsentAction()

		val card = viewModel.stillApplies("mem1")
		assertEquals(true, card.answer)
		assertFalse(card.isPending)
	}

	// A report the service refuses while the sheet asks for the consent says so with its snackbar.
	@Test
	fun aReportRefused_whileTheSheetAsksForConsent_saysSoWithItsSnackbar() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(agentMessage("a1", Text("hola"))))
		fixture.feedback.reportError = IllegalStateException("offline")
		fixture.feedback.reportGate = CompletableDeferred()
		viewModel.openAction(null)
		viewModel.reportMessageAction("a1", AssistantReportReason.INCORRECT, comment = null)
		askConsentByPhoto(fixture, viewModel)

		fixture.feedback.reportGate?.complete(Unit)

		assertEquals(
			listOf("report-failed"),
			fixture.effects.filterIsInstance<AssistantChat.Effect.ShowSnackBar>().map { snack -> snack.message }
		)
	}

	// The student attaches a photo and the service asks for the consent: the sheet is on the consent
	// once the reading ends.
	private fun askConsentByPhoto(fixture: AssistantChatFixture, viewModel: AssistantChatViewModel) {
		val reading = CompletableDeferred<Unit>()
		fixture.extraction.gate = reading
		fixture.extraction.failure = AssistantRequestRefusedException(AssistantErrorCodes.CONSENT_REQUIRED, null, "consent")

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))
		reading.complete(Unit)

		assertEquals(AssistantChat.State.ConsentRequired, viewModel.state.value)
	}

	// A confirmation running while a photo is read, and the photo asking for the consent: the sheet is on the consent with
	// the confirmation still running. The confirmation is held by the test that gates it.
	private fun confirmingWhileAPhotoAsksForConsent(
		fixture: AssistantChatFixture,
		viewModel: AssistantChatViewModel,
		intentId: String
	) {
		val reading = CompletableDeferred<Unit>()
		fixture.extraction.gate = reading
		fixture.extraction.failure = AssistantRequestRefusedException(AssistantErrorCodes.CONSENT_REQUIRED, null, "consent")

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))
		viewModel.confirmIntentAction(intentId)
		reading.complete(Unit)

		assertEquals(AssistantChat.State.ConsentRequired, viewModel.state.value)
	}

	private fun rememberIntent(id: String) = assistantIntent(
		id = id,
		args = RememberArgs(statement = "Trabajo de noche", category = "rutina", quote = "de noche")
	)

	private fun restrictedUntil(untilMillis: Long) = assistantStatus().copy(
		standing = AbuseStanding(level = StandingLevel.SUSPENDED, untilMillis = untilMillis, appealable = true)
	)

	private fun stillAppliesMessage(
		memoryId: String,
		messageId: String = "a1",
		componentId: String = "c1"
	): AssistantConversationItem {
		return agentMessage(
			messageId,
			Component(
				StillAppliesComponent(
					id = componentId,
					fallbackText = "¿Sigue aplicando?",
					memoryId = memoryId,
					statement = "Prefiere las mañanas"
				)
			)
		)
	}

	private fun AssistantChatViewModel.stillApplies(memoryId: String): StillAppliesItem {
		return content().items
			.filterIsInstance<ChatItem.Agent>()
			.flatMap { agent -> agent.parts }
			.filterIsInstance<ChatPartItem.Component>()
			.mapNotNull { part -> part.item as? StillAppliesItem }
			.single { card -> card.memoryId == memoryId }
	}

	private fun memoryList(vararg states: Pair<String, String>): MemoryList {
		return MemoryList(
			items = states.map { (id, state) ->
				MemoryItem(
					id = id,
					category = MemoryVocabulary.CATEGORY_PREFERENCE,
					text = "Prefiere",
					state = state,
					createdAtMillis = 0L
				)
			}
		)
	}

	private fun photo() = PlatformFile("assistant-plan.jpg")
}
