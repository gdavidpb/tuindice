package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
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
import com.gdavidpb.tuindice.base.domain.exception.EncodedImageTooLargeException
import com.gdavidpb.tuindice.chatcontract.abuse.AbuseStanding
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.attachment.AttachmentLimits
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CompletableDeferred
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AssistantChatAttachmentBehaviorTest {
	@Test
	fun when_aPhotoIsPicked_then_itIsReadSentAndKeptAsAReadyAttachment() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)

		addPhotos(viewModel, listOf(photo()))

		val content = viewModel.content()
		val attachment = content.composer.attachments.single()
		assertFalse(attachment.isUploading)
		assertFalse(attachment.isFailed)
		assertEquals(1, fixture.attachmentRepository.uploads.size)
		assertTrue(content.composer.isSendEnabled)
	}

	@Test
	fun when_moreThanFourAreAdded_then_fourAreKeptAndTheLimitIsSaid() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)

		addPhotos(viewModel, List(5) { index -> PlatformFile("foto-$index.jpg") })

		val content = viewModel.content()
		assertEquals(4, content.composer.attachments.size)
		assertEquals(ChatNoticeItem.TurnFailed(message = "attachment-limit", canRetry = false), content.notice)
	}

	@Test
	fun when_fourAttachmentsAreAttached_then_theClipIsOffAndTheLimitIsSaid() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)

		addPhotos(viewModel, List(4) { index -> PlatformFile("foto-$index.jpg") })

		val content = viewModel.content()
		assertFalse(content.composer.isAttachEnabled)
		assertEquals(ChatNoticeItem.TurnFailed(message = "attachment-limit", canRetry = false), content.notice)
	}

	// The photo cannot be read: its read fails with no reason the chat names, which is a failure the retry can read again.
	@Test
	fun when_aPhotoCannotBeRead_then_itFailsWithItsReasonAndTheRetryReadsItAgain() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.imageEncoder.failure = IllegalArgumentException("Image content is not a supported image.")

		addPhotos(viewModel, listOf(PlatformFile("notas.jpg")))

		val failed = viewModel.content().composer.attachments.single()
		assertTrue(failed.isFailed)
		assertTrue(failed.canRetry)
		assertEquals("failure:Other", failed.failureText)
		assertFalse(viewModel.content().composer.isSendEnabled)

		val reads = fixture.imageEncoder.specs.size
		fixture.imageEncoder.failure = null
		viewModel.dispatch(AssistantChat.Action.RetryAttachment(localId = failed.localId))

		assertTrue(fixture.imageEncoder.specs.size > reads)
		assertFalse(viewModel.content().composer.attachments.single().isFailed)
		assertTrue(viewModel.content().composer.isSendEnabled)
	}

	@Test
	fun when_theServiceRefusesAnUploadAsTooLarge_then_theAttachmentFailsWithThatReason() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.attachmentRepository.failure =
			AssistantRequestRefusedException(code = "attachment_too_large", retryAfterSeconds = null, message = "big")

		addPhotos(viewModel, listOf(photo()))

		assertEquals("failure:TooLarge", viewModel.content().composer.attachments.single().failureText)
	}

	@Test
	fun when_aPhotoIsStillTooLargeOnceEncoded_then_itFailsAsTooLargeWithoutRetry() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.imageEncoder.failure = EncodedImageTooLargeException(
			encodedBytes = AttachmentLimits.MAX_IMAGE_BYTES + 1,
			maxBytes = AttachmentLimits.MAX_IMAGE_BYTES
		)

		addPhotos(viewModel, listOf(photo()))

		val failed = viewModel.content().composer.attachments.single()
		assertTrue(failed.isFailed)
		assertFalse(failed.canRetry)
		assertEquals("failure:TooLarge", failed.failureText)
		assertTrue(fixture.attachmentRepository.uploads.isEmpty())
	}

	// The service refuses the send as unavailable: the photo leaves the tray with the failed send, and its retry is
	// offered. A retry refused as a quota gives nothing back: the photo is not in the tray, so the tray stays empty.
	@Test
	fun when_aSendRefusedAsUnavailable_then_aRetryRefusedAsQuotaLeavesTheTrayEmpty() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		addPhotos(viewModel, listOf(photo()))
		fixture.turns.sendError =
			AssistantRequestRefusedException(code = "unavailable", retryAfterSeconds = null, message = "down")

		viewModel.sendMessageAction()

		assertTrue(viewModel.content().composer.attachments.isEmpty())

		fixture.turns.sendError =
			AssistantRequestRefusedException(code = "quota_exhausted", retryAfterSeconds = 30, message = "quota")
		viewModel.dispatch(AssistantChat.Action.RetryTurn)

		assertEquals(2, fixture.turns.requests.size)
		assertTrue(viewModel.content().composer.attachments.isEmpty())
	}

	// Five photos are picked for four places, and two of them are the same picture. The copy leaves the tray, and the
	// room it leaves is room again, so the limit the picks reached is not said any more.
	@Test
	fun when_fiveArePickedWithTwoEqual_then_TheLimitIsNotSaidOnceTheCopyLeaves() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)

		addPhotos(viewModel, listOf("a.jpg", "b.jpg", "c.jpg", "a.jpg", "d.jpg").map { name -> PlatformFile(name) })

		val content = viewModel.content()
		assertEquals(3, content.composer.attachments.size)
		assertEquals(3, fixture.attachmentRepository.uploads.size)
		assertTrue(content.composer.isAttachEnabled)
		assertNotEquals(ChatNoticeItem.TurnFailed(message = "attachment-limit", canRetry = false), content.notice)
	}

	// A sent photo leaves nothing behind in the chat: its read and its upload are over, so what the chat kept for it goes.
	@Test
	fun when_aPhotoIsSent_then_TheChatKeepsNothingForIt() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		addPhotos(viewModel, listOf(photo()))

		viewModel.sendMessageAction()

		assertEquals(listOf("att-1"), fixture.turns.requests.single().message.attachmentIds)
		assertTrue(fixture.machine.turns.attachments.trackedIds().isEmpty())
	}

	@Test
	fun when_anAttachmentIsRemoved_then_itLeavesTheTray() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		addPhotos(viewModel, listOf(photo()))
		val localId = viewModel.content().composer.attachments.single().localId

		viewModel.dispatch(AssistantChat.Action.RemoveAttachment(localId = localId))

		assertTrue(viewModel.content().composer.attachments.isEmpty())
	}

	@Test
	fun when_aMessageHasOnlyAttachments_then_itIsSentWithTheirIdsAndTheTrayClears() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		addPhotos(viewModel, listOf(photo()))

		viewModel.sendMessageAction()

		assertEquals(listOf("att-1"), fixture.turns.requests.single().message.attachmentIds)
		assertTrue(viewModel.content().composer.attachments.isEmpty())
	}

	@Test
	fun when_aSendIsRefused_then_theTextAndTheAttachmentsReturnToTheTray() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		addPhotos(viewModel, listOf(photo()))
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

		addPhotos(viewModel, listOf(photo()))

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

		addPhotos(viewModel, listOf(photo()))

		assertEquals(AssistantChat.State.ConsentRequired, viewModel.state.value)
	}

	@Test
	fun when_aPhotoIsBeingSent_then_theSendWaitsForIt() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		val upload = CompletableDeferred<Unit>()
		fixture.attachmentRepository.gate = upload

		addPhotos(viewModel, listOf(photo()))

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
		addPhotos(viewModel, listOf(photo()))
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

		addPhotos(viewModel, listOf(photo()))

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

		addPhotos(viewModel, listOf(photo()))

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

		addPhotos(viewModel, listOf(photo()))

		assertIs<ChatNoticeItem.Restricted>(viewModel.content().notice)
	}

	@Test
	fun when_anUploadFailsForAReasonThatCanClear_then_itsRetryDoesNotReadThePhotoAgain() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)
		fixture.attachmentRepository.failure = AssistantRequestRefusedException(
			code = "unavailable",
			retryAfterSeconds = null,
			message = "down"
		)
		addPhotos(viewModel, listOf(photo()))
		val failed = viewModel.content().composer.attachments.single()
		assertTrue(failed.canRetry)
		val encodes = fixture.imageEncoder.specs.size

		fixture.attachmentRepository.failure = null
		viewModel.dispatch(AssistantChat.Action.RetryAttachment(localId = failed.localId))

		assertEquals(encodes, fixture.imageEncoder.specs.size)
		assertEquals(2, fixture.attachmentRepository.uploads.size)
		assertFalse(viewModel.content().composer.attachments.single().isFailed)
	}

	@Test
	fun when_theSamePhotoIsPickedAgain_then_theSecondLeavesTheTray() = chatTest { fixture, viewModel ->
		openChat(fixture, viewModel)

		addPhotos(viewModel, listOf(photo()))
		addPhotos(viewModel, listOf(photo()))

		assertEquals(1, viewModel.content().composer.attachments.size)
		assertEquals(1, fixture.attachmentRepository.uploads.size)
	}

	@Test
	fun when_theServiceRefusesTheAttachmentsOfAMessage_then_TheTextAndTheAttachmentsReturnToTheTray() =
		chatTest { fixture, viewModel ->
			openChat(fixture, viewModel)
			addPhotos(viewModel, listOf(photo()))
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
		addPhotos(viewModel, listOf(photo()))
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
		addPhotos(viewModel, listOf(photo()))
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
		addPhotos(viewModel, listOf(photo()))
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

	private fun addPhotos(viewModel: AssistantChatViewModel, files: List<PlatformFile>) {
		viewModel.dispatch(AssistantChat.Action.AddAttachments(files = files))
	}

	private fun photo() = PlatformFile("foto.jpg")

	private companion object {
		const val NOW_MILLIS = 1_700_000_000_000L
		const val ONE_HOUR_MILLIS = 3_600_000L
	}
}
