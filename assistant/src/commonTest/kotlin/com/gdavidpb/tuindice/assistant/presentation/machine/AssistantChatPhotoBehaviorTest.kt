package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantChatFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.intentCard
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.assistant.testing.assistantIntent
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionResult
import com.gdavidpb.tuindice.chatcontract.intent.RememberArgs
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AssistantChatPhotoBehaviorTest {
	@Test
	fun when_aPhotoIsRead_then_theProposalAndItsIntentReachTheChat() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		val intent = assistantIntent(
			id = "i-photo",
			args = RememberArgs(statement = "Parcial 1", category = "evaluaciones", quote = "parcial")
		)
		fixture.extraction.result = ExtractionResult(message = "Encontré una evaluación.", intent = intent)

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))

		val content = viewModel.content()
		assertEquals(listOf("Encontré una evaluación."), content.agentTexts())
		assertEquals(IntentCardStatus.Open, content.intentCard("i-photo").status)
		assertTrue(content.composer.isPhotoEnabled)
	}

	@Test
	fun when_aPhotoIsBeingRead_then_thePhotoButtonIsOffAndNothingOpens() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		val gate = CompletableDeferred<Unit>()
		fixture.extraction.gate = gate

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))

		val reading = viewModel.content()
		assertTrue(reading.isExtracting)
		assertFalse(reading.composer.isPhotoEnabled)

		viewModel.dispatch(AssistantChat.Action.RequestPhoto(fromCamera = true))
		assertFalse(fixture.effects.contains(AssistantChat.Effect.OpenCamera))

		gate.complete(Unit)

		val read = viewModel.content()
		assertEquals(null, read.progressLabel)
		assertFalse(read.isExtracting)
		assertTrue(read.composer.isPhotoEnabled)
	}

	@Test
	fun when_theGalleryIsRequested_then_thePickerOpens() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)

		viewModel.dispatch(AssistantChat.Action.RequestPhoto(fromCamera = false))

		assertEquals(listOf<AssistantChat.Effect>(AssistantChat.Effect.OpenPhotoPicker), fixture.effects)
	}

	@Test
	fun when_theQuotaIsExhaustedByAPhoto_then_theQuotaNoticeShows() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.extraction.failure = AssistantRequestRefusedException(
			code = AssistantErrorCodes.QUOTA_EXHAUSTED,
			retryAfterSeconds = 60,
			message = "quota"
		)

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))

		assertEquals(ChatNoticeItem.QuotaExhausted(message = "quota:60"), viewModel.content().notice)
	}

	@Test
	fun when_theNetworkFailsOnAPhoto_then_theSendFailedNoticeShowsWithoutARetry() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.extraction.failure = IllegalStateException("offline")

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))

		assertEquals(
			ChatNoticeItem.TurnFailed(message = "send-failed", canRetry = false),
			viewModel.content().notice
		)
	}

	@Test
	fun when_theServiceSaysGuidedOnly_then_theChatIsGuidedAndNoPhotoIsRequested() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.extraction.failure = AssistantRequestRefusedException(
			code = AssistantErrorCodes.GUIDED_ONLY,
			retryAfterSeconds = null,
			message = "guided"
		)

		viewModel.dispatch(AssistantChat.Action.AttachPhoto(photo()))
		viewModel.dispatch(AssistantChat.Action.RequestPhoto(fromCamera = false))

		val content = viewModel.content()
		assertTrue(content.isGuided)
		assertFalse(content.composer.isPhotoEnabled)
		assertEquals(ChatNoticeItem.Guided(message = "guided"), content.notice)
		assertFalse(fixture.effects.contains(AssistantChat.Effect.OpenPhotoPicker))
	}

	private suspend fun openChat(fixture: AssistantChatFixture, viewModel: AssistantChatViewModel) {
		fixture.consent.setAssistantConsentGranted(true)
		viewModel.openAction(null)
	}

	private fun photo() = PlatformFile("assistant-plan.jpg")

	private fun AssistantChat.State.Content.agentTexts(): List<String> {
		return items
			.filterIsInstance<ChatItem.Agent>()
			.flatMap { agent -> agent.parts }
			.filterIsInstance<ChatPartItem.Text>()
			.map { part -> part.text }
	}
}
