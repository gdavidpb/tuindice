package com.gdavidpb.tuindice.chatcontract.attachment

import com.gdavidpb.tuindice.chatcontract.ChatContract
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionLimits
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AttachmentContractTest {

	@Test
	fun aNameRoundTripsThroughTheHeader() {
		val name = "Plan de evaluación (1)+ñ 😀.pdf"
		val header = AttachmentName.encode(name)

		assertEquals("Plan%20de%20evaluaci%C3%B3n%20%281%29%2B%C3%B1%20%F0%9F%98%80.pdf", header)
		assertEquals(name, AttachmentName.decode(header))
	}

	@Test
	fun aHeaderThatIsNotPercentEncodedUtf8HasNoName() {
		assertNull(AttachmentName.decode("%E2%28%A1"))
		assertNull(AttachmentName.decode("abc%2"))
		assertNull(AttachmentName.decode("abc%zz"))
		assertNull(AttachmentName.decode("ñ"))
		assertNull(AttachmentName.decode("%20"))
	}

	@Test
	fun aLongNameIsCutByCodePointsWithoutSplittingAPair() {
		val name = "a".repeat(AttachmentLimits.MAX_NAME_LENGTH - 1) + "😀😀"
		val cut = AttachmentName.truncate(name)

		assertEquals("a".repeat(AttachmentLimits.MAX_NAME_LENGTH - 1) + "😀", cut)
		assertEquals(cut, AttachmentName.decode(AttachmentName.encode(name)))
	}

	@Test
	fun anAnswerFromANewerServiceIgnoresWhatItDoesNotKnow() {
		val json = """{"attachmentId":"att-1","mediaType":"image/jpeg","sizeBytes":10,"expiresAtMillis":1,"thumb":"x"}"""

		val accepted = ChatContract.json.decodeFromString(AttachmentAccepted.serializer(), json)

		assertEquals(AttachmentAccepted("att-1", "image/jpeg", 10, null, 1), accepted)
	}

	@Test
	fun theErrorCodesAreDistinct() {
		val codes = listOf(
			AssistantErrorCodes.ASSISTANT_DISABLED, AssistantErrorCodes.CONSENT_REQUIRED,
			AssistantErrorCodes.QUOTA_EXHAUSTED, AssistantErrorCodes.TURN_IN_PROGRESS, AssistantErrorCodes.SUSPENDED,
			AssistantErrorCodes.GUIDED_ONLY, AssistantErrorCodes.INTENT_EXPIRED, AssistantErrorCodes.INTENT_CONFLICT,
			AssistantErrorCodes.APPEAL_NOT_ALLOWED, AssistantErrorCodes.APPEAL_OPEN, AssistantErrorCodes.UNAVAILABLE,
			AssistantErrorCodes.ATTACHMENT_TOO_LARGE, AssistantErrorCodes.ATTACHMENT_UNSUPPORTED,
			AssistantErrorCodes.ATTACHMENT_UNREADABLE, AssistantErrorCodes.ATTACHMENT_EXPIRED,
			AssistantErrorCodes.ATTACHMENT_LIMIT, AssistantErrorCodes.ATTACHMENT_DAILY_LIMIT
		)

		assertEquals(codes.size, codes.toSet().size)
	}

	@Test
	fun theLimitsHoldTogether() {
		assertTrue(AttachmentLimits.PDF_MEDIA_TYPE in AttachmentLimits.MEDIA_TYPES)
		assertTrue(AttachmentLimits.IMAGE_MEDIA_TYPES.all { it in AttachmentLimits.MEDIA_TYPES })
		assertEquals(AttachmentLimits.IMAGE_MEDIA_TYPES, ExtractionLimits.MEDIA_TYPES)
		// A photo as base64 still fits what an extraction accepts.
		assertTrue((AttachmentLimits.MAX_IMAGE_BYTES + 2) / 3 * 4 <= ExtractionLimits.MAX_IMAGE_BASE64_BYTES)
		// The largest single attachment fits in what one message allows.
		assertTrue(AttachmentLimits.MAX_PDF_BYTES <= AttachmentLimits.MAX_BYTES_PER_MESSAGE)
	}
}
