package com.gdavidpb.tuindice.assistant.presentation.machine.collaborator

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachment
import com.gdavidpb.tuindice.assistant.domain.usecase.CancelAssistantTurnUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.FollowAssistantTurnUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ResumeAssistantTurnUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.SendAssistantMessageUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.error.SendAssistantMessageError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.FollowAssistantTurnParams
import com.gdavidpb.tuindice.assistant.domain.usecase.param.SendAssistantMessageParams
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import com.gdavidpb.tuindice.chatcontract.event.RunFinished
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect

/**
 * The turns: the resumption and the follow of a turn, its recovery, and the send. Each command
 * reports its outcome to the machine as an internal event; what the outcome means is the machine's to decide.
 */
class AssistantChatTurns(
	private val conversation: AssistantChatConversation,
	private val resumeAssistantTurnUseCase: ResumeAssistantTurnUseCase,
	private val followAssistantTurnUseCase: FollowAssistantTurnUseCase,
	private val sendAssistantMessageUseCase: SendAssistantMessageUseCase,
	private val cancelAssistantTurnUseCase: CancelAssistantTurnUseCase,
	val attachments: AssistantChatAttachments
) {
	private var turnJob: Job? = null

	// The conversation is observed by its collaborator, which starts the observation once per chat.
	fun observeConversation(host: MachineHost<AssistantChat.Effect>) {
		conversation.observe(host)
	}

	// The turn that was running when the chat opened is followed again from the seq it had reached.
	fun resumeTurn(host: MachineHost<AssistantChat.Effect>) {
		turnJob?.cancel()
		turnJob = host.launchMachineJob {
			resumeAssistantTurnUseCase.execute(Unit).collect { useCaseState ->
				reportTurnState(host, useCaseState)
			}
		}
	}

	fun followTurn(host: MachineHost<AssistantChat.Effect>, turnId: String, afterSeq: Long) {
		turnJob?.cancel()
		turnJob = host.launchMachineJob {
			val params = FollowAssistantTurnParams(turnId = turnId, afterSeq = afterSeq)
			var finished = false

			followAssistantTurnUseCase.execute(params).collect { useCaseState ->
				val event = (useCaseState as? UseCaseState.Data)?.value

				if (event is RunFinished || event is RunError) finished = true

				reportTurnState(host, useCaseState)
			}

			// A follow that ends without the turn's final event, whether the stream closed or broke, leaves the turn's result
			// unknown: the send that waits for it is told.
			if (!finished) host.processInternalEvent(AssistantChatInternalEvent.TurnStreamClosed(turnId = turnId))
		}
	}

	// Follows the unconfirmed turn [turnId] again, from the seq it reached, as the resumption does, to learn its
	// result. Its final event confirms the turn; a stream that ends or breaks before it leaves the turn unconfirmed.
	fun recoverTurn(host: MachineHost<AssistantChat.Effect>, turnId: String) {
		turnJob?.cancel()
		turnJob = host.launchMachineJob {
			var confirmed = false

			resumeAssistantTurnUseCase.execute(Unit).collect { useCaseState ->
				val event = (useCaseState as? UseCaseState.Data)?.value

				if (event is RunFinished || event is RunError) confirmed = true

				reportTurnState(host, useCaseState)
			}

			host.processInternalEvent(
				if (confirmed) {
					AssistantChatInternalEvent.TurnRecovered(turnId = turnId)
				} else {
					AssistantChatInternalEvent.TurnNotRecovered(turnId = turnId)
				}
			)
		}
	}

	fun send(
		host: MachineHost<AssistantChat.Effect>,
		params: SendAssistantMessageParams,
		sentText: String,
		restoreText: String?,
		suggestionId: String?
	) {
		host.launchMachineJob {
			sendAssistantMessageUseCase.execute(params).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						AssistantChatInternalEvent.TurnAccepted(
							turnId = useCaseState.value.turnId,
							lastSeq = useCaseState.value.lastSeq,
							suggestionId = suggestionId,
							text = sentText,
							clientTurnId = params.clientTurnId,
							attachments = params.attachments
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						useCaseState.error.toSendEvent(sentText, restoreText, suggestionId, params.clientTurnId, params.attachments)
					)
				}
			}
		}
	}

	// A successful cancellation is terminal for the local stream: the chat must stop offering Stop before the next event.
	fun cancel(host: MachineHost<AssistantChat.Effect>, turnId: String) {
		val turnJobAtRequest = turnJob

		host.launchMachineJob {
			cancelAssistantTurnUseCase.execute(turnId).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					if (turnJob === turnJobAtRequest) {
						turnJobAtRequest?.cancel()
						turnJob = null
					}

					host.processInternalEvent(
						AssistantChatInternalEvent.TurnRunFailed(
							turnId = turnId,
							code = RunErrorCode.CANCELLED,
							retryable = false
						)
					)
				}
			}
		}
	}

	fun stop() {
		conversation.stop()
		turnJob?.cancel()
	}

	// A RunError is the failure the turn reported; its retry flag is the turn's own.
	private fun reportTurnState(
		host: MachineHost<AssistantChat.Effect>,
		useCaseState: UseCaseState<AssistantEvent, Nothing>
	) {
		when (useCaseState) {
			is UseCaseState.Loading -> Unit

			is UseCaseState.Data -> {
				val event = useCaseState.value

				if (event is RunError) {
					host.processInternalEvent(
						AssistantChatInternalEvent.TurnRunFailed(
							turnId = event.turnId,
							code = event.code,
							retryable = event.retryable
						)
					)
				}
			}

			// A broken stream leaves the turn unconfirmed in the conversation, which is where the student sees it.
			is UseCaseState.Error -> Unit
		}
	}
}

private fun SendAssistantMessageError?.toSendEvent(
	sentText: String,
	restoreText: String?,
	suggestionId: String?,
	clientTurnId: String,
	attachments: List<AssistantAttachment>
): AssistantChatInternalEvent {
	return when (this) {
		is SendAssistantMessageError.AssistantDisabled -> AssistantChatInternalEvent.SendDisabled

		SendAssistantMessageError.ConsentRequired -> AssistantChatInternalEvent.SendConsentRequired

		else -> AssistantChatInternalEvent.SendRejected(
			error = this,
			sentText = sentText,
			restoreText = restoreText,
			suggestionId = suggestionId,
			clientTurnId = clientTurnId,
			restoreAttachments = attachments
		)
	}
}
