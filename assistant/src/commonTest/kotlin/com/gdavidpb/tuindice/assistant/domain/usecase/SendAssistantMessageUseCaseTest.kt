package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentContent
import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachmentState
import com.gdavidpb.tuindice.assistant.domain.usecase.error.SendAssistantMessageError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.SendAssistantMessageExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.SendAssistantMessageParams
import com.gdavidpb.tuindice.assistant.domain.usecase.validator.SendAssistantMessageParamsValidator
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantClientStateRepository
import com.gdavidpb.tuindice.assistant.testing.FixedClock
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantConversationRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantTurnRepository
import com.gdavidpb.tuindice.assistant.testing.lastState
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.component.ChatCatalogs
import com.gdavidpb.tuindice.chatcontract.context.ClientState
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.chatcontract.context.ScreenContext
import com.gdavidpb.tuindice.chatcontract.context.ScreenId
import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SendAssistantMessageUseCaseTest {
	private val calls = mutableListOf<String>()
	private val conversation = RecordingAssistantConversationRepository(calls)
	private val turns = RecordingAssistantTurnRepository(calls)

	private fun useCase() = SendAssistantMessageUseCase(
		conversationRepository = conversation,
		turnRepository = turns,
		clientStateRepository = FakeAssistantClientStateRepository(ClientState(recordRevision = 3)),
		clock = FixedClock(nowMillis = NOW_MILLIS),
		reportingRepository = RecordingReportingRepository(),
		handling = SendAssistantMessageHandling(
			paramsValidator = SendAssistantMessageParamsValidator(),
			exceptionHandler = SendAssistantMessageExceptionHandler()
		)
	)

	private suspend fun send(
		text: String?,
		suggestionId: String? = null,
		screenContext: AssistantScreenContext? = null
	) = useCase().execute(
		SendAssistantMessageParams(text = text, suggestionId = suggestionId, screenContext = screenContext)
	).lastState()

	@Test
	fun storesTheMessageBeforeTheRequestAndAttachesTheConversationAfterIt() = runTest {
		turns.acceptedTurn = TurnAccepted(turnId = "t1", conversationId = "c9", lastSeq = 0)

		val state = send(text = "Hola")

		assertEquals(listOf("addUserMessage", "sendTurn", "attachConversation"), calls)
		assertEquals(listOf("c9"), conversation.attached)
		assertEquals(UseCaseState.Data(turns.acceptedTurn), state)
	}

	@Test
	fun aRetryCarriesTheClientTurnIdOfTheSendItRetries() = runTest {
		useCase().execute(
			SendAssistantMessageParams(
				text = "Hola",
				suggestionId = null,
				screenContext = null,
				clientTurnId = "failed-1"
			)
		).lastState()

		assertEquals(listOf("failed-1"), conversation.userMessages.map { message -> message.id })
		assertEquals("failed-1", turns.requests.single().clientTurnId)
	}

	@Test
	fun theRequestCarriesTheIdsOfTheStoredMessageAndTheContractValues() = runTest {
		conversation.conversationId = "c1"
		val context = AssistantScreenContext(
			screen = AssistantScreen.EVALUATIONS,
			entities = listOf(AssistantEntityRef(kind = AssistantEntityKind.EVALUATION, id = "e1")),
			visibleFields = mapOf("nota" to "15"),
			filters = mapOf("periodo" to "2026-1")
		)

		send(text = "  Hola  ", screenContext = context)

		val request = turns.requests.single()
		val stored = conversation.userMessages.single()

		assertEquals(stored.id, request.clientTurnId)
		assertEquals("Hola", stored.text)
		assertEquals(NOW_MILLIS, stored.createdAtMillis)
		assertEquals("Hola", request.message.text)
		assertEquals("c1", request.conversationId)
		assertEquals(ClientState(recordRevision = 3), request.clientState)
		assertEquals(ChatCatalogs.supported, request.supportedCatalogs)
		assertEquals(
			ScreenContext(
				screen = ScreenId.EVALUATIONS,
				entities = listOf(EntityRef(kind = EntityKind.EVALUATION, id = "e1")),
				visibleFields = mapOf("nota" to "15"),
				filters = mapOf("periodo" to "2026-1"),
				locale = "es-VE",
				timeZone = "America/Caracas"
			),
			request.screenContext
		)
	}

	@Test
	fun aSuggestionAloneIsAValidMessageWithoutText() = runTest {
		send(text = "   ", suggestionId = "s1")

		val request = turns.requests.single()

		assertEquals(null, request.message.text)
		assertEquals("s1", request.message.suggestionId)
	}

	@Test
	fun anEmptyMessageOrOneOverTheLimitIsRefusedBeforeAnythingIsStored() = runTest {
		assertEquals(UseCaseState.Error(SendAssistantMessageError.InvalidMessage), send(text = " \n "))
		assertEquals(UseCaseState.Error(SendAssistantMessageError.InvalidMessage), send(text = null))
		assertEquals(UseCaseState.Error(SendAssistantMessageError.InvalidMessage), send(text = "a".repeat(2_001)))

		assertTrue(conversation.userMessages.isEmpty())
		assertTrue(turns.requests.isEmpty())
	}

	@Test
	fun aMessageOfExactlyTheLimitIsSent() = runTest {
		send(text = "a".repeat(2_000))

		assertEquals(1, turns.requests.size)
	}

	@Test
	fun aRefusedSendKeepsTheMessageAndReadsTheRefusalAsATypedError() = runTest {
		turns.sendError = AssistantRequestRefusedException(
			code = "quota_exhausted",
			retryAfterSeconds = 30,
			message = "quota"
		)

		val state = send(text = "Hola")

		assertEquals(UseCaseState.Error(SendAssistantMessageError.QuotaExhausted(retryAfterSeconds = 30)), state)
		assertEquals(1, conversation.userMessages.size)
		assertTrue(conversation.attached.isEmpty())
	}

	@Test
	fun eachRefusalCodeReadsAsItsTypedErrorAndAnUnknownOneAsOther() = runTest {
		val expected = listOf(
			"assistant_disabled" to SendAssistantMessageError.AssistantDisabled(retryAfterSeconds = 120),
			"consent_required" to SendAssistantMessageError.ConsentRequired,
			"suspended" to SendAssistantMessageError.Restricted,
			"guided_only" to SendAssistantMessageError.GuidedOnly,
			"turn_in_progress" to SendAssistantMessageError.TurnInProgress,
			"unavailable" to SendAssistantMessageError.Other
		)

		expected.forEach { (code, error) ->
			turns.sendError = AssistantRequestRefusedException(code = code, retryAfterSeconds = 120, message = code)

			assertEquals(UseCaseState.Error(error), send(text = "Hola"), code)
		}
	}

	@Test
	fun aFailureThatIsNotARefusalIsAnUnnamedError() = runTest {
		turns.sendError = IllegalStateException("sin red")

		assertEquals(UseCaseState.Error<SendAssistantMessageError>(null), send(text = "Hola"))
	}

	// A retry takes the question it replaces out in the write that stores its own: the two are one step, so the question
	// of the retry is never kept next to the one it replaces.
	@Test
	fun aRetryReplacesItsQuestion_inOneWrite_thatStoresItsOwn() = runTest {
		useCase().execute(
			SendAssistantMessageParams(text = "Hola", suggestionId = null, screenContext = null, clientTurnId = "failed-1")
		).lastState()
		calls.clear()

		useCase().execute(
			SendAssistantMessageParams(
				text = "Hola",
				suggestionId = null,
				screenContext = null,
				clientTurnId = "retry-1",
				replacesMessageId = "failed-1",
				replacesSessionMark = 0
			)
		).lastState()

		assertEquals(listOf("addUserMessage", "sendTurn", "attachConversation"), calls)
		assertEquals(listOf("retry-1"), conversation.userMessages.map { message -> message.id })
		assertEquals(listOf("failed-1" to 0), conversation.replacements)
	}

	@Test
	fun aMessageOfPhotosAloneIsSentWithTheirIdsAndKeepsThemInTheConversation() = runTest {
		val first = readyAttachment(localId = "l1", attachmentId = "att-1")
		val second = readyAttachment(localId = "l2", attachmentId = "att-2")

		val state = sendWithAttachments(text = null, attachments = listOf(first, second))

		val request = turns.requests.single()
		val stored = conversation.userMessages.single().attachments

		assertTrue(state is UseCaseState.Data<*>)
		assertNull(request.message.text)
		assertEquals(listOf("att-1", "att-2"), request.message.attachmentIds)
		assertEquals(listOf("l1.jpg", "l2.jpg"), stored.map { attachment -> attachment.name })
		assertContentEquals(byteArrayOf(1, 2, 3), stored.first().image)
		assertContentEquals(byteArrayOf(1, 2, 3), stored.last().image)
	}

	@Test
	fun aMessageWithTextAndAttachmentsCarriesBoth() = runTest {
		sendWithAttachments(
			text = "¿Cuánto vale cada parcial?",
			attachments = listOf(readyAttachment(localId = "l1", attachmentId = "att-1"))
		)

		val message = turns.requests.single().message

		assertEquals("¿Cuánto vale cada parcial?", message.text)
		assertEquals(listOf("att-1"), message.attachmentIds)
	}

	@Test
	fun aMessageOfFourAttachmentsIsSent() = runTest {
		val four = (1..4).map { index -> readyAttachment("l$index", "att-$index") }

		sendWithAttachments(text = null, attachments = four)

		assertEquals(listOf("att-1", "att-2", "att-3", "att-4"), turns.requests.single().message.attachmentIds)
	}

	@Test
	fun aMessageOfMoreThanFourAttachmentsOrOneNotKeptIsRefusedBeforeAnythingIsStored() = runTest {
		val five = (1..5).map { index -> readyAttachment("l$index", "att-$index") }
		val notKept = readyAttachment("l1", "att-1")
			.copy(state = AssistantAttachmentState.Uploading)

		assertEquals(
			UseCaseState.Error(SendAssistantMessageError.InvalidMessage),
			sendWithAttachments(text = null, attachments = five)
		)
		assertEquals(
			UseCaseState.Error(SendAssistantMessageError.InvalidMessage),
			sendWithAttachments(text = null, attachments = listOf(notKept))
		)

		assertTrue(conversation.userMessages.isEmpty())
		assertTrue(turns.requests.isEmpty())
	}

	private fun readyAttachment(
		localId: String,
		attachmentId: String
	) = AssistantAttachment(
		localId = localId,
		name = "$localId.jpg",
		thumbnail = byteArrayOf(9),
		state = AssistantAttachmentState.Ready(attachmentId),
		content = AssistantAttachmentContent(mediaType = "image/jpeg", sha256 = "", bytes = byteArrayOf(1, 2, 3))
	)

	private suspend fun sendWithAttachments(
		text: String?,
		attachments: List<AssistantAttachment>
	) = useCase().execute(
		SendAssistantMessageParams(text = text, suggestionId = null, screenContext = null, attachments = attachments)
	).lastState()

	private companion object {
		const val NOW_MILLIS = 1_700_000_000_000L
	}
}
