package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.exception.AssistantRequestRefusedException
import com.gdavidpb.tuindice.assistant.domain.usecase.error.SendAssistantMessageError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.SendAssistantMessageParams
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
import kotlin.test.assertEquals
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
		reportingRepository = RecordingReportingRepository()
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

	private companion object {
		const val NOW_MILLIS = 1_700_000_000_000L
	}
}
