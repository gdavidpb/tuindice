package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentKind
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.testing.AssistantChatFixture
import com.gdavidpb.tuindice.assistant.presentation.testing.changeTextAction
import com.gdavidpb.tuindice.assistant.presentation.testing.chatTest
import com.gdavidpb.tuindice.assistant.presentation.testing.content
import com.gdavidpb.tuindice.assistant.presentation.testing.openAction
import com.gdavidpb.tuindice.assistant.presentation.testing.sendMessageAction
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantChatViewModel
import com.gdavidpb.tuindice.assistant.testing.assistantStatus
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentAccepted
import io.github.vinceglb.filekit.PlatformFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AssistantChatAttachmentBehaviorTest {
	@Test
	fun when_aPhotoIsPicked_then_itIsReadSentAndKeptAsAReadyAttachment() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)

		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)

		val content = viewModel.content()
		val attachment = content.composer.attachments.single()
		assertFalse(attachment.isUploading)
		assertFalse(attachment.isFailed)
		assertEquals(1, fixture.attachmentRepository.uploads.size)
		assertTrue(content.composer.isSendEnabled)
	}

	@Test
	fun when_aPdfIsPicked_then_itsPagesShowOnceTheServiceHasReadThem() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.attachmentFiles.bytes = PDF_BYTES
		fixture.attachmentRepository.accepted = AttachmentAccepted(
			attachmentId = "att-pdf",
			mediaType = "application/pdf",
			sizeBytes = PDF_BYTES.size.toLong(),
			pageCount = 12,
			expiresAtMillis = NOW_MILLIS,
			name = "programa.pdf"
		)

		addFiles(viewModel, listOf(PlatformFile("programa.pdf")), AssistantAttachmentKind.PDF)

		assertEquals("pages:12", viewModel.content().composer.attachments.single().detail)
	}

	@Test
	fun when_moreThanFourAreAdded_then_fourAreKeptAndTheLimitIsSaid() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)

		addFiles(viewModel, List(5) { index -> PlatformFile("foto-$index.jpg") }, AssistantAttachmentKind.PHOTO)

		val content = viewModel.content()
		assertEquals(4, content.composer.attachments.size)
		assertEquals(ChatNoticeItem.TurnFailed(message = "attachment-limit", canRetry = false), content.notice)
	}

	@Test
	fun when_aFileCannotBeRead_then_itFailsWithItsReasonAndTheRetryReadsItAgain() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.attachmentFiles.bytes = "texto sin cabecera".encodeToByteArray()

		addFiles(viewModel, listOf(PlatformFile("notas.pdf")), AssistantAttachmentKind.PDF)

		val failed = viewModel.content().composer.attachments.single()
		assertTrue(failed.isFailed)
		assertEquals("failure:Unsupported", failed.failureText)
		assertFalse(viewModel.content().composer.isSendEnabled)

		fixture.attachmentFiles.bytes = PDF_BYTES
		viewModel.dispatch(AssistantChat.Action.RetryAttachment(localId = failed.localId))

		val retried = viewModel.content().composer.attachments.single()
		assertFalse(retried.isFailed)
		assertTrue(viewModel.content().composer.isSendEnabled)
	}

	@Test
	fun when_theServiceRefusesAnUploadAsTooLarge_then_theAttachmentFailsWithThatReason() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.attachmentRepository.failure =
			AssistantRequestRefusedException(code = "attachment_too_large", retryAfterSeconds = null, message = "big")

		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)

		assertEquals("failure:TooLarge", viewModel.content().composer.attachments.single().failureText)
	}

	@Test
	fun when_anAttachmentIsRemoved_then_itLeavesTheTray() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)
		val localId = viewModel.content().composer.attachments.single().localId

		viewModel.dispatch(AssistantChat.Action.RemoveAttachment(localId = localId))

		assertTrue(viewModel.content().composer.attachments.isEmpty())
	}

	@Test
	fun when_aMessageHasOnlyAttachments_then_itIsSentWithTheirIdsAndTheTrayClears() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)

		viewModel.sendMessageAction()

		assertEquals(listOf("att-1"), fixture.turns.requests.single().message.attachmentIds)
		assertTrue(viewModel.content().composer.attachments.isEmpty())
	}

	@Test
	fun when_aSendIsRefused_then_theTextAndTheAttachmentsReturnToTheTray() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)
		viewModel.changeTextAction("¿Cuánto vale cada parcial?")
		fixture.turns.sendError =
			AssistantRequestRefusedException(code = "quota_exhausted", retryAfterSeconds = 30, message = "q")

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertEquals("¿Cuánto vale cada parcial?", content.composer.text)
		assertEquals(1, content.composer.attachments.size)
	}

	@Test
	fun when_theServiceKeepsAttachmentsOff_then_theClipIsHiddenAndNothingIsAdded() = chatTest { fixture, viewModel ->
		fixture.consent.setAssistantConsentGranted(true)
		fixture.standing.answer = assistantStatus()
		viewModel.openAction(null)

		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)

		val composer = viewModel.content().composer
		assertFalse(composer.showsAttach)
		assertFalse(composer.isAttachEnabled)
		assertTrue(composer.attachments.isEmpty())
	}

	@Test
	fun when_anUploadNeedsTheConsent_then_theChatAsksForIt() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.attachmentRepository.failure =
			AssistantRequestRefusedException(code = "consent_required", retryAfterSeconds = null, message = "consent")

		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)

		assertEquals(AssistantChat.State.ConsentRequired, viewModel.state.value)
	}

	private suspend fun openChat(fixture: AssistantChatFixture, viewModel: AssistantChatViewModel) {
		fixture.consent.setAssistantConsentGranted(true)
		fixture.standing.answer = assistantStatus().copy(attachmentsEnabled = true)
		viewModel.openAction(null)
	}

	private fun addFiles(viewModel: AssistantChatViewModel, files: List<PlatformFile>, kind: AssistantAttachmentKind) {
		viewModel.dispatch(AssistantChat.Action.AddAttachments(files = files, kind = kind))
	}

	private fun photo() = PlatformFile("foto.jpg")

	private companion object {
		val PDF_BYTES = "%PDF-1.7\n%prueba\n".encodeToByteArray()
		const val NOW_MILLIS = 1_700_000_000_000L
	}
}
