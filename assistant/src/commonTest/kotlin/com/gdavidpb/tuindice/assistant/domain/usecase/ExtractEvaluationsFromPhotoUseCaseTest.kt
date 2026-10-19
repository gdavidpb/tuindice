package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.usecase.error.ExtractEvaluationsFromPhotoError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.ExtractEvaluationsFromPhotoExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ExtractEvaluationsFromPhotoParams
import com.gdavidpb.tuindice.assistant.presentation.testing.NOW_MILLIS
import com.gdavidpb.tuindice.assistant.presentation.testing.ObservedAssistantConversationRepository
import com.gdavidpb.tuindice.assistant.presentation.testing.emptyAssistantConversation
import com.gdavidpb.tuindice.assistant.testing.FixedClock
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantExtractionRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingImageEncoderRepository
import com.gdavidpb.tuindice.assistant.testing.assistantIntent
import com.gdavidpb.tuindice.base.domain.model.ImageEncodingSpec
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.error.AssistantErrorCodes
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionLimits
import com.gdavidpb.tuindice.chatcontract.extraction.ExtractionResult
import com.gdavidpb.tuindice.chatcontract.intent.RememberArgs
import com.gdavidpb.tuindice.chatcontract.message.IntentPart
import com.gdavidpb.tuindice.chatcontract.message.MessagePart
import com.gdavidpb.tuindice.chatcontract.message.Text
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid

class ExtractEvaluationsFromPhotoUseCaseTest {
	private val file = PlatformFile("assistant-plan.jpg")

	@Test
	fun when_aPhotoIsRead_then_itIsEncodedToTheLimitsOfTheService() = runTest {
		val encoder = RecordingImageEncoderRepository()

		useCase(imageEncoder = encoder).execute(params()).toList()

		assertEquals(
			listOf(
				ImageEncodingSpec(
					maxDimensionPx = ExtractionLimits.MAX_IMAGE_EDGE_PX,
					maxBytes = 3_500_000,
					jpegQuality = 80
				)
			),
			encoder.specs
		)
	}

	@Test
	fun when_aPhotoIsRead_then_theRequestCarriesTheBase64ImageAndItsMediaType() = runTest {
		val encoder = RecordingImageEncoderRepository().apply { content = byteArrayOf(0x10, 0x20, 0x30) }
		val extraction = RecordingAssistantExtractionRepository()

		useCase(imageEncoder = encoder, extraction = extraction).execute(params()).toList()

		val request = extraction.requests.single()
		assertEquals(Base64.Default.encode(byteArrayOf(0x10, 0x20, 0x30)), request.imageBase64)
		assertEquals("image/jpeg", request.mediaType)
	}

	@Test
	fun when_theSamePhotoIsReadAgain_then_itsClientRequestIdIsTheSame() = runTest {
		val extraction = RecordingAssistantExtractionRepository()
		val useCase = useCase(extraction = extraction)

		useCase.execute(params()).toList()
		useCase.execute(params()).toList()

		val (first, second) = extraction.requests.map { request -> request.clientRequestId }
		assertEquals(first, second)
		assertEquals('8', first[14], "the id is a version 8 UUID: $first")
	}

	@Test
	fun when_anotherPhotoIsRead_then_itsClientRequestIdIsAnotherOne() = runTest {
		val encoder = RecordingImageEncoderRepository()
		val extraction = RecordingAssistantExtractionRepository()
		val useCase = useCase(imageEncoder = encoder, extraction = extraction)

		useCase.execute(params()).toList()
		encoder.content = byteArrayOf(9, 9, 9)
		useCase.execute(params()).toList()

		val (first, second) = extraction.requests.map { request -> request.clientRequestId }
		assertNotEquals(first, second)
		Uuid.parse(second)
	}

	@Test
	fun when_aPhotoIsRead_then_theScreenContextAndTheCurrentConversationAreSent() = runTest {
		val conversation = ObservedAssistantConversationRepository(
			initial = emptyAssistantConversation().copy(conversationId = "c-7")
		)
		val extraction = RecordingAssistantExtractionRepository()
		val context = AssistantScreenContext(
			screen = AssistantScreen.EVALUATIONS,
			visibleFields = mapOf("subject" to "CI4325")
		)

		useCase(extraction = extraction, conversation = conversation)
			.execute(params(context = context))
			.toList()

		val request = extraction.requests.single()
		assertEquals("c-7", request.conversationId)
		assertNotNull(request.screenContext)
	}

	@Test
	fun when_aPhotoIsRead_then_theProposalReachesTheConversationWithItsIntent() = runTest {
		val conversation = ObservedAssistantConversationRepository()
		val intent = assistantIntent(
			id = "i-photo",
			args = RememberArgs(statement = "Parcial 1", category = "evaluaciones", quote = "parcial")
		)
		val extraction = RecordingAssistantExtractionRepository().apply {
			result = ExtractionResult(message = "Encontré una evaluación.", intent = intent)
		}

		useCase(extraction = extraction, conversation = conversation).execute(params()).toList()

		val written = conversation.conversation.value
		val agent = assertIs<AssistantConversationItem.Agent>(written.items.single())
		val parts: List<MessagePart> = listOf(Text("Encontré una evaluación."), IntentPart(intent))
		assertEquals(parts, agent.message.parts)
		assertEquals(intent, written.openIntents.getValue("i-photo"))
	}

	@Test
	fun when_theQuotaIsExhausted_then_theErrorCarriesTheWait() = runTest {
		val extraction = RecordingAssistantExtractionRepository().apply {
			failure = AssistantRequestRefusedException(
				code = AssistantErrorCodes.QUOTA_EXHAUSTED,
				retryAfterSeconds = 90,
				message = "quota"
			)
		}

		assertEquals(
			ExtractEvaluationsFromPhotoError.QuotaExhausted(retryAfterSeconds = 90),
			errorOf(useCase(extraction = extraction))
		)
	}

	@Test
	fun when_theServiceRefuses_then_theErrorNamesTheRefusal() = runTest {
		val refusals = mapOf(
			AssistantErrorCodes.ASSISTANT_DISABLED to ExtractEvaluationsFromPhotoError.AssistantDisabled(30),
			AssistantErrorCodes.CONSENT_REQUIRED to ExtractEvaluationsFromPhotoError.ConsentRequired,
			AssistantErrorCodes.SUSPENDED to ExtractEvaluationsFromPhotoError.Restricted,
			AssistantErrorCodes.GUIDED_ONLY to ExtractEvaluationsFromPhotoError.GuidedOnly,
			"invalid_image" to ExtractEvaluationsFromPhotoError.Other
		)

		for ((code, expected) in refusals) {
			val extraction = RecordingAssistantExtractionRepository().apply {
				failure = AssistantRequestRefusedException(code = code, retryAfterSeconds = 30, message = code)
			}

			assertEquals(expected, errorOf(useCase(extraction = extraction)), code)
		}
	}

	@Test
	fun when_theNetworkFails_then_thereIsNoRefusalToName() = runTest {
		val extraction = RecordingAssistantExtractionRepository().apply {
			failure = IllegalStateException("offline")
		}

		assertEquals(null, errorOf(useCase(extraction = extraction)))
	}

	private fun params(context: AssistantScreenContext? = null): ExtractEvaluationsFromPhotoParams {
		return ExtractEvaluationsFromPhotoParams(file = file, screenContext = context)
	}

	private fun useCase(
		imageEncoder: RecordingImageEncoderRepository = RecordingImageEncoderRepository(),
		extraction: RecordingAssistantExtractionRepository = RecordingAssistantExtractionRepository(),
		conversation: ObservedAssistantConversationRepository = ObservedAssistantConversationRepository()
	): ExtractEvaluationsFromPhotoUseCase {
		return ExtractEvaluationsFromPhotoUseCase(
			imageEncoderRepository = imageEncoder,
			extractionRepository = extraction,
			conversationRepository = conversation,
			clock = FixedClock(nowMillis = NOW_MILLIS),
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = ExtractEvaluationsFromPhotoExceptionHandler()
		)
	}

	private suspend fun errorOf(useCase: ExtractEvaluationsFromPhotoUseCase): ExtractEvaluationsFromPhotoError? {
		return when (val last = useCase.execute(params()).toList().last()) {
			is UseCaseState.Error -> last.error

			else -> error("the reading did not fail: $last")
		}
	}
}
