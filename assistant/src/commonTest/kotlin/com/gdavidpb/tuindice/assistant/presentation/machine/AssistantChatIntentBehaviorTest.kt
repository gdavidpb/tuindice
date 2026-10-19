package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.IntentDecisionResult
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AssistantIntentOutcome
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantDraft
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.confirmIntentAction
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.dismissIntentAction
import com.gdavidpb.tuindice.assistant.presentation.testing.intentCard
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.testing.toggleIntentDraftAction
import com.gdavidpb.tuindice.assistant.testing.assistantIntent
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.IntentRisk
import com.gdavidpb.tuindice.chatcontract.intent.RememberArgs
import com.gdavidpb.tuindice.chatcontract.intent.RemoveEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.RequestEnrollmentProofArgs
import com.gdavidpb.tuindice.chatcontract.message.IntentPart
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssistantChatIntentBehaviorTest {
	@Test
	fun confirmingAnIntent_showsItRunning_andThenResolved() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		val intent = assistantIntent(
			id = "i1",
			args = RememberArgs(statement = "Trabajo de noche", category = "rutina", quote = "de noche")
		)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a1", IntentPart(intent))),
			openIntents = mapOf("i1" to intent)
		)
		fixture.gatedIntents.gated = true
		viewModel.openAction(null)

		viewModel.confirmIntentAction("i1")
		assertEquals(IntentCardStatus.Running, viewModel.content().intentCard("i1").status)

		fixture.gatedIntents.release.complete(Unit)
		assertEquals(IntentCardStatus.Resolved(AssistantIntentOutcome.CONFIRMED), viewModel.content().intentCard("i1").status)
	}

	@Test
	fun destructiveIntent_isMarkedAsSuch_andConfirmedByItsOwnAction() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		val args = RemoveEvaluationArgs(mutationId = "m2", evaluationId = "e2")
		val intent = assistantIntent(id = "i2", args = args, mutationIds = listOf("m2")).copy(risk = IntentRisk.DESTRUCTIVE)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a2", IntentPart(intent))),
			openIntents = mapOf("i2" to intent)
		)
		fixture.gatedIntents.gated = true
		viewModel.openAction(null)
		assertTrue(viewModel.content().intentCard("i2").isDestructive)

		viewModel.confirmIntentAction("i2")
		assertEquals(IntentCardStatus.Running, viewModel.content().intentCard("i2").status)

		fixture.gatedIntents.release.complete(Unit)

		assertEquals(listOf<Any>(args), fixture.evaluations.received)
		assertEquals(IntentCardStatus.Resolved(AssistantIntentOutcome.CONFIRMED), viewModel.content().intentCard("i2").status)
	}

	@Test
	fun enrollmentProof_opensTheAppsOwnFlow() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		val intent = assistantIntent(id = "i3", args = RequestEnrollmentProofArgs).copy(risk = IntentRisk.CREDENTIAL)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a3", IntentPart(intent))),
			openIntents = mapOf("i3" to intent)
		)
		viewModel.openAction(null)
		assertTrue(viewModel.content().intentCard("i3").usesCredential)

		viewModel.confirmIntentAction("i3")

		assertEquals(listOf<AssistantChat.Effect>(AssistantChat.Effect.OpenEnrollmentProof), fixture.effects)
		assertEquals(IntentCardStatus.Resolved(AssistantIntentOutcome.DELEGATED), viewModel.content().intentCard("i3").status)
	}

	@Test
	fun anExpiredEnrollmentProof_opensNoFlow_andIsResolvedAsExpired() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		val intent = assistantIntent(id = "i4", args = RequestEnrollmentProofArgs, expiresAtMillis = 1L)
			.copy(risk = IntentRisk.CREDENTIAL)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a4", IntentPart(intent))),
			openIntents = mapOf("i4" to intent)
		)
		viewModel.openAction(null)

		viewModel.confirmIntentAction("i4")

		assertTrue(fixture.effects.isEmpty())
		assertEquals(IntentCardStatus.Resolved(AssistantIntentOutcome.EXPIRED), viewModel.content().intentCard("i4").status)
	}

	@Test
	fun aConflictingEnrollmentProof_opensNoFlow_andIsResolvedAsConflict() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.intents.answer = IntentDecisionResult.Conflict
		val intent = assistantIntent(id = "i5", args = RequestEnrollmentProofArgs).copy(risk = IntentRisk.CREDENTIAL)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a5", IntentPart(intent))),
			openIntents = mapOf("i5" to intent)
		)
		viewModel.openAction(null)

		viewModel.confirmIntentAction("i5")

		assertTrue(fixture.effects.isEmpty())
		assertEquals(IntentCardStatus.Resolved(AssistantIntentOutcome.CONFLICT), viewModel.content().intentCard("i5").status)
	}

	@Test
	fun batchRows_unmarkedBeforeConfirming_areSentAsEditedArgs() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		val batch = AddEvaluationsArgs(items = listOf(assistantDraft("m1", "Parcial 1"), assistantDraft("m2", "Quiz 2")))
		val intent = assistantIntent(id = "i4", args = batch)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a4", IntentPart(intent))),
			openIntents = mapOf("i4" to intent)
		)
		viewModel.openAction(null)

		val card = viewModel.content().intentCard("i4")
		assertEquals(listOf(true, true), card.drafts.map { draft -> draft.selected })
		assertEquals("type:QUIZ · undated · max:20.0", card.drafts.first().detail)

		viewModel.toggleIntentDraftAction("i4", "m2", selected = false)
		assertEquals(listOf(true, false), viewModel.content().intentCard("i4").drafts.map { draft -> draft.selected })

		viewModel.confirmIntentAction("i4")

		val sent = fixture.evaluations.received.single() as AddEvaluationsArgs
		assertEquals(listOf("m1" to true, "m2" to false), sent.items.map { item -> item.mutationId to item.selected })
	}

	@Test
	fun dismissingAnIntent_cancelsIt() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		val intent = assistantIntent(id = "i5", args = RememberArgs(statement = "x", category = "y", quote = "z"))
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a5", IntentPart(intent))),
			openIntents = mapOf("i5" to intent)
		)
		viewModel.openAction(null)

		viewModel.dismissIntentAction("i5")

		assertEquals(false, fixture.intents.decisions.single().accepted)
		assertEquals(IntentCardStatus.Resolved(AssistantIntentOutcome.CANCELLED), viewModel.content().intentCard("i5").status)
	}
}
