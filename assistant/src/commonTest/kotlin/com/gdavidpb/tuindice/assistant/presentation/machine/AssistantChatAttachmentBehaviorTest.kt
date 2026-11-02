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
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentAccepted
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
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

	@Test
	fun when_aPhotoIsBeingSent_then_theSendWaitsForIt() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		val upload = CompletableDeferred<Unit>()
		fixture.attachmentRepository.gate = upload

		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)

		val sending = viewModel.content().composer.attachments.single()
		assertTrue(sending.isUploading)
		assertFalse(viewModel.content().composer.isSendEnabled)

		upload.complete(Unit)

		assertFalse(viewModel.content().composer.attachments.single().isUploading)
		assertTrue(viewModel.content().composer.isSendEnabled)
	}

	@Test
	fun when_anAttachmentIsRemovedWhileItIsSent_then_itDoesNotComeBackWhenTheSendEnds() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		val upload = CompletableDeferred<Unit>()
		fixture.attachmentRepository.gate = upload
		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)
		val localId = viewModel.content().composer.attachments.single().localId

		viewModel.dispatch(AssistantChat.Action.RemoveAttachment(localId = localId))
		upload.complete(Unit)

		assertTrue(viewModel.content().composer.attachments.isEmpty())
		assertEquals(1, fixture.attachmentRepository.uploads.size)
	}

	@Test
	fun when_theServiceSaysADailyLimitWasUsed_then_theAttachmentSaysSoAndIsNotRetried() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.attachmentRepository.failure = AssistantRequestRefusedException(
			code = "attachment_daily_limit",
			retryAfterSeconds = 3600,
			message = "daily"
		)

		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)

		val failed = viewModel.content().composer.attachments.single()
		assertTrue(failed.isFailed)
		assertFalse(failed.canRetry)
		assertEquals("failure:DailyLimit(retryAfterSeconds=3600)", failed.failureText)
	}

	@Test
	fun when_theServiceSaysTheGuidedModeIsOn_then_theChatIsGuided() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		// The chat reads the status again after the refusal: the server is the one that says the mode is guided.
		fixture.standing.answer = assistantStatus().copy(attachmentsEnabled = true, mode = "guided")
		fixture.attachmentRepository.failure = AssistantRequestRefusedException(
			code = "guided_only",
			retryAfterSeconds = null,
			message = "guided"
		)

		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)

		assertEquals(ChatNoticeItem.Guided(message = "guided"), viewModel.content().notice)
		assertTrue(viewModel.content().composer.attachments.single().isFailed)
	}

	@Test
	fun when_theServiceSaysTheStudentIsRestricted_then_theChatShowsTheRestriction() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.clock.nowMillis = NOW_MILLIS
		// The chat reads the standing again after the refusal: the server is the one that says the restriction holds.
		fixture.standing.answer = assistantStatus().copy(
			attachmentsEnabled = true,
			standing = AbuseStanding(level = StandingLevel.COOLDOWN, untilMillis = NOW_MILLIS + ONE_HOUR_MILLIS)
		)
		fixture.attachmentRepository.failure = AssistantRequestRefusedException(
			code = "suspended",
			retryAfterSeconds = null,
			message = "suspended"
		)

		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)

		assertIs<ChatNoticeItem.Restricted>(viewModel.content().notice)
	}

	@Test
	fun when_anUploadFailsForAReasonThatCanClear_then_itsRetryDoesNotReadTheFileAgain() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.attachmentFiles.bytes = PDF_BYTES
		fixture.attachmentRepository.failure = AssistantRequestRefusedException(
			code = "unavailable",
			retryAfterSeconds = null,
			message = "down"
		)
		addFiles(viewModel, listOf(PlatformFile("programa.pdf")), AssistantAttachmentKind.PDF)
		val failed = viewModel.content().composer.attachments.single()
		assertTrue(failed.canRetry)

		fixture.attachmentRepository.failure = null
		viewModel.dispatch(AssistantChat.Action.RetryAttachment(localId = failed.localId))

		assertEquals(1, fixture.attachmentFiles.reads.size)
		assertEquals(2, fixture.attachmentRepository.uploads.size)
		assertFalse(viewModel.content().composer.attachments.single().isFailed)
	}

	@Test
	fun when_theSamePhotoIsPickedAgain_then_theSecondLeavesTheTray() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)

		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)
		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)

		assertEquals(1, viewModel.content().composer.attachments.size)
		assertEquals(1, fixture.attachmentRepository.uploads.size)
	}

	@Test
	fun when_fourAttachmentsAreAttached_then_theClipIsOffAndTheLimitIsSaid() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)

		addFiles(viewModel, List(4) { index -> PlatformFile("foto-$index.jpg") }, AssistantAttachmentKind.PHOTO)

		val content = viewModel.content()
		assertFalse(content.composer.isAttachEnabled)
		assertEquals(ChatNoticeItem.TurnFailed(message = "attachment-limit", canRetry = false), content.notice)
	}

	@Test
	fun when_theMessageWouldWeighMoreThanAllowed_then_TheFileThatTipsItIsNotSent() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.attachmentFiles.bytes = PDF_HEADER + ByteArray(NINE_MEGABYTES)
		addFiles(viewModel, listOf(PlatformFile("uno.pdf")), AssistantAttachmentKind.PDF)
		fixture.attachmentFiles.bytes = PDF_HEADER + ByteArray(NINE_MEGABYTES) { 1 }

		addFiles(viewModel, listOf(PlatformFile("dos.pdf")), AssistantAttachmentKind.PDF)

		val second = viewModel.content().composer.attachments[1]
		assertEquals("failure:MessageTooLarge", second.failureText)
		assertFalse(second.canRetry)
		assertEquals(1, fixture.attachmentRepository.uploads.size)
	}

	@Test
	fun when_theServiceRefusesTheAttachmentsOfAMessage_then_TheTextAndTheAttachmentsReturnToTheTray() =
		chatTest { fixture, viewModel ->
			openChat(fixture, viewModel)
			addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)
			viewModel.changeTextAction("¿Qué pesa más?")
			fixture.turns.sendError =
				AssistantRequestRefusedException(code = "attachment_limit", retryAfterSeconds = null, message = "limit")

			viewModel.sendMessageAction()

			val content = viewModel.content()
			assertEquals("¿Qué pesa más?", content.composer.text)
			assertEquals(1, content.composer.attachments.size)
			assertEquals(ChatNoticeItem.TurnFailed(message = "attachments-refused", canRetry = false), content.notice)
		}

	@Test
	fun when_AnAttachmentOfAMessageExpired_then_TheTextReturnsAndTheAttachmentsLeave() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)
		viewModel.changeTextAction("¿Qué pesa más?")
		fixture.turns.sendError =
			AssistantRequestRefusedException(code = "attachment_expired", retryAfterSeconds = null, message = "expired")

		viewModel.sendMessageAction()

		val content = viewModel.content()
		assertEquals("¿Qué pesa más?", content.composer.text)
		assertTrue(content.composer.attachments.isEmpty())
		assertEquals(ChatNoticeItem.TurnFailed(message = "attachment-expired", canRetry = false), content.notice)
	}

	@Test
	fun when_aRetryIsAccepted_then_TheAttachmentsLeaveTheTrayTheyWereSentFrom() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)
		viewModel.changeTextAction("¿Qué pesa más?")
		fixture.turns.sendError =
			AssistantRequestRefusedException(code = "turn_in_progress", retryAfterSeconds = null, message = "busy")
		viewModel.sendMessageAction()
		assertEquals(1, viewModel.content().composer.attachments.size)

		fixture.turns.sendError = null
		viewModel.dispatch(AssistantChat.Action.RetryTurn)

		assertEquals(listOf("att-1"), fixture.turns.requests.last().message.attachmentIds)
		assertTrue(viewModel.content().composer.attachments.isEmpty())
	}

	@Test
	fun when_aRetryIsRefused_then_TheAttachmentComesBackOnce() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		addFiles(viewModel, listOf(photo()), AssistantAttachmentKind.PHOTO)
		val localId = viewModel.content().composer.attachments.single().localId
		viewModel.changeTextAction("¿Qué pesa más?")
		fixture.turns.sendError =
			AssistantRequestRefusedException(code = "turn_in_progress", retryAfterSeconds = null, message = "busy")
		viewModel.sendMessageAction()

		fixture.turns.sendError =
			AssistantRequestRefusedException(code = "quota_exhausted", retryAfterSeconds = 30, message = "quota")
		viewModel.dispatch(AssistantChat.Action.RetryTurn)

		assertEquals(listOf(localId), viewModel.content().composer.attachments.map { attachment -> attachment.localId })
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
		val PDF_HEADER = "%PDF-".encodeToByteArray()
		const val NOW_MILLIS = 1_700_000_000_000L
		const val ONE_HOUR_MILLIS = 3_600_000L
		const val NINE_MEGABYTES = 9 * 1024 * 1024
	}
}
