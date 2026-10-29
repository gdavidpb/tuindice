package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.AssistantReportReason
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import com.gdavidpb.tuindice.assistant.presentation.testing.agentMessage
import com.gdavidpb.tuindice.assistant.presentation.testing.answerStillAppliesAction
import com.gdavidpb.tuindice.assistant.presentation.testing.appealAction
import com.gdavidpb.tuindice.assistant.presentation.testing.assistantConversation
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.copyMessageAction
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.testing.openComponentSheetAction
import com.gdavidpb.tuindice.assistant.presentation.testing.openNavTargetAction
import com.gdavidpb.tuindice.assistant.presentation.testing.rateMessageAction
import com.gdavidpb.tuindice.assistant.presentation.testing.reportMessageAction
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.component.StillAppliesComponent
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.chatcontract.feedback.MessageFeedback
import com.gdavidpb.tuindice.chatcontract.feedback.ReportReason
import com.gdavidpb.tuindice.chatcontract.memory.MemoryItem
import com.gdavidpb.tuindice.chatcontract.memory.MemoryList
import com.gdavidpb.tuindice.chatcontract.memory.MemoryVocabulary
import com.gdavidpb.tuindice.chatcontract.message.Component
import com.gdavidpb.tuindice.chatcontract.message.Text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AssistantChatMessageBehaviorTest {
	@Test
	fun copy_handsTheTextOfTheMessageToTheDevice() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(agentMessage("a1", Text("Tu promedio es 15,4")))
		)
		viewModel.openAction(null)

		viewModel.copyMessageAction("a1")

		val copied = fixture.effects.single() as AssistantChat.Effect.CopyText
		assertEquals("Tu promedio es 15,4", copied.text)
	}

	@Test
	fun rate_recordsTheRating_andThanksTheStudent() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(agentMessage("a1", Text("hola"))))
		viewModel.openAction(null)

		viewModel.rateMessageAction("a1", useful = true)

		assertEquals(listOf(MessageFeedback(messageId = "a1", useful = true)), fixture.feedback.rated)
		assertEquals(true, (viewModel.content().items.single() as ChatItem.Agent).useful)
		val snack = fixture.effects.single() as AssistantChat.Effect.ShowSnackBar
		assertEquals("rated", snack.message)
	}

	@Test
	fun rate_refusedByTheService_putsTheThumbsBack_andSaysSo() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(agentMessage("a1", Text("hola"))))
		viewModel.openAction(null)
		fixture.feedback.rateError = IllegalStateException("offline")

		viewModel.rateMessageAction("a1", useful = true)

		assertEquals(null, (viewModel.content().items.single() as ChatItem.Agent).useful)
		val snack = fixture.effects.single() as AssistantChat.Effect.ShowSnackBar
		assertEquals("rate-failed", snack.message)
	}

	@Test
	fun report_sendsTheReason_andConfirmsIt() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(agentMessage("a1", Text("hola"))))
		viewModel.openAction(null)

		viewModel.reportMessageAction("a1", AssistantReportReason.INCORRECT, comment = "  no es así ")

		val report = fixture.feedback.reported.single()
		assertEquals(ReportReason.INCORRECT, report.reason)
		assertEquals("no es así", report.comment)
		val snack = fixture.effects.single() as AssistantChat.Effect.ShowSnackBar
		assertEquals("reported", snack.message)
	}

	@Test
	fun report_savedByTheService_marksTheMessageAsReported() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(agentMessage("a1", Text("hola"))))
		viewModel.openAction(null)

		viewModel.reportMessageAction("a1", AssistantReportReason.INCORRECT, comment = null)

		assertEquals(true, (viewModel.content().items.single() as ChatItem.Agent).reported)
	}

	@Test
	fun report_refusedByTheService_leavesTheMessageAsItWas_andSaysSo() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(agentMessage("a1", Text("hola"))))
		viewModel.openAction(null)
		fixture.feedback.reportError = IllegalStateException("offline")

		viewModel.reportMessageAction("a1", AssistantReportReason.INCORRECT, comment = null)

		assertEquals(false, (viewModel.content().items.single() as ChatItem.Agent).reported)
		val snack = fixture.effects.single() as AssistantChat.Effect.ShowSnackBar
		assertEquals("report-failed", snack.message)
	}

	@Test
	fun report_ofAMessageAlreadyReported_isNotSentAgain() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(agentMessage("a1", Text("hola"))))
		viewModel.openAction(null)

		viewModel.reportMessageAction("a1", AssistantReportReason.INCORRECT, comment = null)
		viewModel.reportMessageAction("a1", AssistantReportReason.HARMFUL, comment = null)

		assertEquals(1, fixture.feedback.reported.size)
		assertEquals(1, fixture.effects.size, "only the first report says something")
	}

	@Test
	fun navigate_opensTheScreen_andLeavesTheClosingOfTheSheetToTheJump() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
		val refs = listOf(EntityRef(kind = EntityKind.TERM, id = "2026-2"))

		viewModel.openNavTargetAction(NavTarget.SUMMARY, refs)

		// One effect: the jump closes the sheet itself, and a Dismiss after it would close the tab it landed on.
		val navigation = fixture.effects.single() as AssistantChat.Effect.NavigateTo
		assertEquals(NavTarget.SUMMARY, navigation.target)
		assertEquals(refs, navigation.entityRefs)
	}

	@Test
	fun componentSheet_opensTheComponentOnItsOwnSheet() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)

		viewModel.openComponentSheetAction("a1", "c1")

		val sheet = fixture.effects.single() as AssistantChat.Effect.OpenComponentSheet
		assertEquals("a1", sheet.messageId)
		assertEquals("c1", sheet.componentId)
	}

	@Test
	fun externalLink_opensTheLinkWithTheSystem() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)

		viewModel.dispatch(AssistantChat.Action.OpenExternalLink(url = "https://example.com/reglamento"))

		val open = fixture.effects.single() as AssistantChat.Effect.OpenUrl
		assertEquals("https://example.com/reglamento", open.url)
	}

	@Test
	fun dismiss_closesTheSheet() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)

		viewModel.dispatch(AssistantChat.Action.Dismiss)

		assertEquals(listOf<AssistantChat.Effect>(AssistantChat.Effect.Dismiss), fixture.effects)
	}

	@Test
	fun appeal_sendsTheText_andConfirmsIt() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)

		viewModel.appealAction("  Fue un error  ")

		assertEquals(listOf("Fue un error"), fixture.standing.appeals.map { appeal -> appeal.text })
		val snack = fixture.effects.single() as AssistantChat.Effect.ShowSnackBar
		assertEquals("appeal-sent", snack.message)
	}

	@Test
	fun stillApplies_notSaved_saysSo() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		fixture.memories.answerError = IllegalStateException("offline")

		viewModel.answerStillAppliesAction("mem1", applies = false)

		val snack = fixture.effects.single() as AssistantChat.Effect.ShowSnackBar
		assertEquals("still-applies-failed", snack.message)
	}

	@Test
	fun stillApplies_isAnsweredForTheMemory() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)

		viewModel.answerStillAppliesAction("mem1", applies = false)

		assertEquals(listOf("mem1" to false), fixture.memories.answers)
		assertTrue(fixture.effects.isEmpty())
	}

	@Test
	fun stillApplies_savedByTheService_keepsTheAnswerOnTheCard() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		viewModel.openAction(null)

		viewModel.answerStillAppliesAction("mem1", applies = true)

		val card = viewModel.stillApplies("mem1")
		assertEquals(true, card.answer)
		assertEquals(false, card.isPending)
	}

	@Test
	fun stillApplies_secondTapWhileTheFirstIsSaved_isNotSentAgain() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		viewModel.openAction(null)
		fixture.gatedMemories.gated = true

		viewModel.answerStillAppliesAction("mem1", applies = false)
		viewModel.answerStillAppliesAction("mem1", applies = true)

		assertEquals(true, viewModel.stillApplies("mem1").isPending)
		fixture.gatedMemories.release.complete(Unit)
		assertEquals(listOf("mem1" to false), fixture.memories.answers)
	}

	@Test
	fun stillApplies_refusedWhileTheMemoryStillWaits_isAskedAgainAndSaysSo() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		viewModel.openAction(null)
		fixture.memories.answerError = IllegalStateException("offline")

		viewModel.answerStillAppliesAction("mem1", applies = false)

		val snack = fixture.effects.single() as AssistantChat.Effect.ShowSnackBar
		assertEquals("still-applies-failed", snack.message)
		assertNull(viewModel.stillApplies("mem1").answer)
		assertEquals(false, viewModel.stillApplies("mem1").isPending)
	}

	@Test
	fun stillApplies_refusedWhileTheMemoryIsActive_keepsTheAnswerWithoutSayingSo() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		viewModel.openAction(null)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_ACTIVE)
		fixture.memories.answerError = IllegalStateException("offline")

		viewModel.answerStillAppliesAction("mem1", applies = false)

		// The answer was tried and refused, so the memory was read again: it is active now.
		assertEquals(2, fixture.memories.listCalls)
		assertTrue(fixture.effects.isEmpty())
		assertEquals(true, viewModel.stillApplies("mem1").answer)
	}

	@Test
	fun stillApplies_observedWithAnActiveMemory_isShownAsStillApplying() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_ACTIVE)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))

		viewModel.openAction(null)

		assertEquals(true, viewModel.stillApplies("mem1").answer)
	}

	@Test
	fun stillApplies_cardArrivingAfterTheFirstRead_asksForANewRead() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		viewModel.openAction(null)
		assertEquals(1, fixture.memories.listCalls)

		fixture.memories.memories = memoryList(
			"mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION,
			"mem2" to MemoryVocabulary.STATE_PENDING_CONFIRMATION
		)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(stillAppliesMessage("mem1"), stillAppliesMessage("mem2", messageId = "a2", componentId = "c2"))
		)

		assertEquals(2, fixture.memories.listCalls)
		assertNull(viewModel.stillAppliesCard("c2").answer)
	}

	@Test
	fun stillApplies_readThatFailed_leavesTheCardOpen_andANewCardAsksForANewRead() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.listError = IllegalStateException("offline")
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		viewModel.openAction(null)
		assertEquals(1, fixture.memories.listCalls)

		// The card stays open, with its buttons, and an observation of the same cards asks for no read.
		assertNull(viewModel.stillAppliesCard("c1").answer)
		assertEquals(false, viewModel.stillAppliesCard("c1").isPending)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(stillAppliesMessage("mem1"), agentMessage("a2", Text("hola")))
		)
		assertEquals(1, fixture.memories.listCalls)

		// A new card asks for a read, which the list now answers.
		fixture.memories.listError = null
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_ACTIVE)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(
				stillAppliesMessage("mem1"),
				agentMessage("a2", Text("hola")),
				stillAppliesMessage("mem1", messageId = "a3", componentId = "c2")
			)
		)

		assertEquals(2, fixture.memories.listCalls)
		assertEquals(true, viewModel.stillAppliesCard("c2").answer)
	}

	@Test
	fun stillApplies_refusedAndTheRereadFails_saysSoAndEnablesTheButtons() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		viewModel.openAction(null)
		fixture.memories.answerError = IllegalStateException("offline")
		fixture.memories.listError = IllegalStateException("offline")

		viewModel.answerStillAppliesAction("mem1", applies = false)

		val snack = fixture.effects.single() as AssistantChat.Effect.ShowSnackBar
		assertEquals("still-applies-failed", snack.message)
		assertNull(viewModel.stillApplies("mem1").answer)
		assertEquals(false, viewModel.stillApplies("mem1").isPending)
	}

	@Test
	fun stillApplies_refusedAndTheMemoryIsGone_saysNoAndSaysNothingMore() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		viewModel.openAction(null)
		fixture.memories.memories = memoryList()
		fixture.memories.answerError = IllegalStateException("offline")

		viewModel.answerStillAppliesAction("mem1", applies = true)

		assertTrue(fixture.effects.isEmpty())
		assertEquals(false, viewModel.stillApplies("mem1").answer)
		assertEquals(false, viewModel.stillApplies("mem1").isPending)
	}

	@Test
	fun stillApplies_readAnsweredLate_doesNotOverwriteANewerRead() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		fixture.gatedMemories.holdNextList = true
		viewModel.openAction(null)

		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_ACTIVE)
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(stillAppliesMessage("mem1"), stillAppliesMessage("mem1", messageId = "a2", componentId = "c2"))
		)
		assertEquals(2, fixture.memories.listCalls)
		assertEquals(true, viewModel.stillAppliesCard("c2").answer)

		// The first read, which saw the memory still waiting, comes back after the newer one.
		fixture.gatedMemories.releaseHeldList.complete(Unit)

		assertEquals(true, viewModel.stillAppliesCard("c1").answer)
	}

	@Test
	fun stillApplies_newCardOfAnAnsweredMemory_asksAgainWhenTheServerStillWaits() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		viewModel.openAction(null)
		viewModel.answerStillAppliesAction("mem1", applies = true)
		assertEquals(true, viewModel.stillAppliesCard("c1").answer)

		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(stillAppliesMessage("mem1"), stillAppliesMessage("mem1", messageId = "a2", componentId = "c2"))
		)

		assertNull(viewModel.stillAppliesCard("c2").answer)
		viewModel.answerStillAppliesAction("mem1", applies = false)
		assertEquals(listOf("mem1" to true, "mem1" to false), fixture.memories.answers)
		assertEquals(false, viewModel.stillAppliesCard("c2").answer)
	}

	@Test
	fun stillApplies_newCardWhoseReadFails_waitsThenAsksWithoutSayingSo() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_ACTIVE)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		viewModel.openAction(null)
		assertEquals(true, viewModel.stillAppliesCard("c1").answer)

		fixture.gatedMemories.holdNextList = true
		fixture.gatedMemories.heldListError = IllegalStateException("offline")
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(stillAppliesMessage("mem1"), stillAppliesMessage("mem1", messageId = "a2", componentId = "c2"))
		)

		// While its read is held the new card waits: no answer, disabled buttons, and a tap is ignored.
		assertNull(viewModel.stillAppliesCard("c2").answer)
		assertEquals(true, viewModel.stillAppliesCard("c2").isPending)
		viewModel.answerStillAppliesAction("mem1", applies = false)
		assertTrue(fixture.memories.answers.isEmpty())

		// The read fails: the card stays open, with its buttons, and nothing is said.
		fixture.gatedMemories.releaseHeldList.complete(Unit)

		assertNull(viewModel.stillAppliesCard("c2").answer)
		assertEquals(false, viewModel.stillAppliesCard("c2").isPending)
		assertTrue(fixture.effects.isEmpty())
	}

	@Test
	fun stillApplies_refusalWaitsForItsOwnRead_whileAnotherReadFails() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		viewModel.openAction(null)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_ACTIVE)
		fixture.memories.answerError = IllegalStateException("offline")
		fixture.gatedMemories.holdNextList = true

		// The refusal asks for the read of its memory, which is held.
		viewModel.answerStillAppliesAction("mem1", applies = false)
		assertEquals(true, viewModel.stillAppliesCard("c1").isPending)

		// A new card of the same memory asks for another read, which fails first.
		fixture.memories.listError = IllegalStateException("offline")
		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(stillAppliesMessage("mem1"), stillAppliesMessage("mem1", messageId = "a2", componentId = "c2"))
		)
		assertTrue(fixture.effects.isEmpty())
		assertEquals(true, viewModel.stillAppliesCard("c1").isPending)

		// The read of the refusal comes back with the memory active: nothing is said, and nothing waits any more.
		fixture.gatedMemories.releaseHeldList.complete(Unit)

		assertTrue(fixture.effects.isEmpty())
		assertEquals(false, viewModel.stillAppliesCard("c1").isPending)
	}

	@Test
	fun stillApplies_listFailingAlways_isNotAskedAgainByTheSameCards() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.listError = IllegalStateException("offline")
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		viewModel.openAction(null)

		fixture.conversation.conversation.value = assistantConversation(
			items = listOf(stillAppliesMessage("mem1"), agentMessage("a2", Text("hola")))
		)

		assertEquals(1, fixture.memories.listCalls)
	}

	@Test
	fun stillApplies_refusedWhileItsRereadIsHeld_isPendingAndIgnoresASecondTap() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.memories.memories = memoryList("mem1" to MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		fixture.conversation.conversation.value = assistantConversation(items = listOf(stillAppliesMessage("mem1")))
		viewModel.openAction(null)
		fixture.memories.answerError = IllegalStateException("offline")
		fixture.gatedMemories.holdNextList = true

		viewModel.answerStillAppliesAction("mem1", applies = false)
		assertEquals(true, viewModel.stillApplies("mem1").isPending)

		viewModel.answerStillAppliesAction("mem1", applies = true)
		assertEquals(2, fixture.memories.listCalls, "the second tap asks for nothing")

		fixture.gatedMemories.releaseHeldList.complete(Unit)
		assertEquals(false, viewModel.stillApplies("mem1").isPending)
	}

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

	private fun AssistantChatViewModel.stillAppliesCard(componentId: String): StillAppliesItem {
		return content().items
			.filterIsInstance<ChatItem.Agent>()
			.flatMap { agent -> agent.parts }
			.filterIsInstance<ChatPartItem.Component>()
			.mapNotNull { part -> part.item as? StillAppliesItem }
			.single { card -> card.id == componentId }
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
}
