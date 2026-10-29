package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.mapper.toScreenContext
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantClientStateRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantConversationRepository
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantTurnRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.error.SendAssistantMessageError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.SendAssistantMessageParams
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.domain.usecase.base.ParamsValidator
import com.gdavidpb.tuindice.chatcontract.component.ChatCatalogs
import com.gdavidpb.tuindice.chatcontract.turn.TurnAccepted
import com.gdavidpb.tuindice.chatcontract.turn.TurnRequest
import com.gdavidpb.tuindice.chatcontract.turn.UserMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Clock

/**
 * Starts a turn with the user's message, stored before the request leaves, so a failed send keeps it for a retry. The
 * message is stored under the client turn id of the params, which is the id the request carries. A retry also takes the
 * question it replaces out of the conversation, in the same send and before its own question is stored.
 */
class SendAssistantMessageUseCase(
	private val conversationRepository: AssistantConversationRepository,
	private val turnRepository: AssistantTurnRepository,
	private val clientStateRepository: AssistantClientStateRepository,
	private val clock: Clock,
	override val reportingRepository: ReportingRepository,
	handling: SendAssistantMessageHandling
) : FlowUseCase<SendAssistantMessageParams, TurnAccepted, SendAssistantMessageError>() {
	override val paramsValidator: ParamsValidator<SendAssistantMessageParams> = handling.paramsValidator

	override val exceptionHandler: ExceptionHandler<SendAssistantMessageError> = handling.exceptionHandler

	override suspend fun executeOnBackground(params: SendAssistantMessageParams): Flow<TurnAccepted> = flow {
		// The session the send starts in: the conversation id the server answers with belongs to it.
		val sessionMark = conversationRepository.sessionMark()
		val text = params.text?.trim()?.takeIf { it.isNotEmpty() }
		val clientTurnId = params.clientTurnId

		// A retry's question takes the question it replaces out in the same write that stores it, so the two are one step.
		conversationRepository.addUserMessage(
			AssistantUserMessage(
				id = clientTurnId,
				text = text.orEmpty(),
				createdAtMillis = clock.now().toEpochMilliseconds()
			),
			replacesMessageId = params.replacesMessageId,
			replacesSessionMark = params.replacesSessionMark
		)

		val request = TurnRequest(
			conversationId = conversationRepository.currentConversation().conversationId,
			clientTurnId = clientTurnId,
			message = UserMessage(text = text, suggestionId = params.suggestionId),
			screenContext = params.screenContext?.toScreenContext(),
			clientState = clientStateRepository.clientState(),
			supportedCatalogs = ChatCatalogs.supported
		)

		val accepted = turnRepository.sendTurn(request)

		conversationRepository.attachConversation(accepted.conversationId, sessionMark)

		emit(accepted)
	}
}
