package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.usecase.ExtractEvaluationsFromPhotoUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.FollowAssistantTurnUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ObserveAssistantConversationUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ResumeAssistantTurnUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.SendAssistantMessageUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.error.ExtractEvaluationsFromPhotoError
import com.gdavidpb.tuindice.assistant.domain.usecase.error.SendAssistantMessageError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ExtractEvaluationsFromPhotoParams
import com.gdavidpb.tuindice.assistant.domain.usecase.param.FollowAssistantTurnParams
import com.gdavidpb.tuindice.assistant.domain.usecase.param.SendAssistantMessageParams
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent
import com.gdavidpb.tuindice.chatcontract.event.RunError
import com.gdavidpb.tuindice.chatcontract.event.RunErrorCode
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect

/**
 * The conversation and the turns: the observation, the resumption and the follow of a turn, and the send. Each command
 * reports its outcome to the machine as an internal event; what the outcome means is the machine's to decide.
 */
class AssistantChatTurns(
	private val observeAssistantConversationUseCase: ObserveAssistantConversationUseCase,
	private val resumeAssistantTurnUseCase: ResumeAssistantTurnUseCase,
	private val followAssistantTurnUseCase: FollowAssistantTurnUseCase,
	private val sendAssistantMessageUseCase: SendAssistantMessageUseCase,
	private val extractEvaluationsFromPhotoUseCase: ExtractEvaluationsFromPhotoUseCase
) {
	private var conversationJob: Job? = null
	private var turnJob: Job? = null
	private var photoJob: Job? = null

	// Started once per sheet: a second observer would feed the same machine twice.
	fun observeConversation(host: MachineHost<AssistantChat.Effect>) {
		if (conversationJob?.isActive == true) return

		conversationJob = host.launchMachineJob {
			observeAssistantConversationUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						AssistantChatInternalEvent.ConversationObserved(conversation = useCaseState.value)
					)
				}
			}
		}
	}

	// The turn that was running when the sheet opened is followed again from the seq it had reached.
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

			followAssistantTurnUseCase.execute(params).collect { useCaseState ->
				reportTurnState(host, useCaseState)
			}
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
							lastSeq = useCaseState.value.lastSeq
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						useCaseState.error.toSendEvent(sentText, restoreText, suggestionId)
					)
				}
			}
		}
	}

	// Reads the photo [file] into the conversation. Its outcome comes back as an event, and its notices are the turn's.
	fun extractPhoto(host: MachineHost<AssistantChat.Effect>, file: PlatformFile, context: AssistantScreenContext?) {
		val params = ExtractEvaluationsFromPhotoParams(file = file, screenContext = context)

		photoJob = host.launchMachineJob {
			extractEvaluationsFromPhotoUseCase.execute(params).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(AssistantChatInternalEvent.PhotoExtracted)

					is UseCaseState.Error -> host.processInternalEvent(useCaseState.error.toPhotoEvent())
				}
			}
		}
	}

	fun stop() {
		conversationJob?.cancel()
		turnJob?.cancel()
		photoJob?.cancel()
	}

	// A RunError is the failure the turn reported. A stream that breaks is a failure that does not invite a retry.
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
						AssistantChatInternalEvent.TurnRunFailed(code = event.code, retryable = event.retryable)
					)
				}
			}

			is UseCaseState.Error -> host.processInternalEvent(
				AssistantChatInternalEvent.TurnRunFailed(code = RunErrorCode.OTHER, retryable = false)
			)
		}
	}

	// The two outcomes a turn's send has keep their own rows, so a photo shows the same screens as a message does.
	private fun ExtractEvaluationsFromPhotoError?.toPhotoEvent(): AssistantChatInternalEvent {
		return when (this) {
			is ExtractEvaluationsFromPhotoError.AssistantDisabled -> AssistantChatInternalEvent.SendDisabled

			ExtractEvaluationsFromPhotoError.ConsentRequired -> AssistantChatInternalEvent.SendConsentRequired

			else -> AssistantChatInternalEvent.PhotoRejected(error = this)
		}
	}

	private fun SendAssistantMessageError?.toSendEvent(
		sentText: String,
		restoreText: String?,
		suggestionId: String?
	): AssistantChatInternalEvent {
		return when (this) {
			is SendAssistantMessageError.AssistantDisabled -> AssistantChatInternalEvent.SendDisabled

			SendAssistantMessageError.ConsentRequired -> AssistantChatInternalEvent.SendConsentRequired

			else -> AssistantChatInternalEvent.SendRejected(
				error = this,
				sentText = sentText,
				restoreText = restoreText,
				suggestionId = suggestionId
			)
		}
	}
}
