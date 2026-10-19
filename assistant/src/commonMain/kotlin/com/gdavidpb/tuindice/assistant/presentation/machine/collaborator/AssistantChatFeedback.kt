package com.gdavidpb.tuindice.assistant.presentation.machine.collaborator

import com.gdavidpb.tuindice.assistant.domain.model.AssistantInputLimits
import com.gdavidpb.tuindice.assistant.domain.usecase.AnswerStillAppliesUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.RateAssistantMessageUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ReportAssistantMessageUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.error.ReportAssistantMessageError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.AnswerStillAppliesParams
import com.gdavidpb.tuindice.assistant.domain.usecase.param.RateAssistantMessageParams
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ReportAssistantMessageParams
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatFeedbackTexts
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.chatcontract.feedback.ReportReason
import kotlinx.coroutines.flow.collect

// What the student says about the answers: the rating and the report of a message, and the answer about a memory.
class AssistantChatFeedback(
	private val rateAssistantMessageUseCase: RateAssistantMessageUseCase,
	private val reportAssistantMessageUseCase: ReportAssistantMessageUseCase,
	private val answerStillAppliesUseCase: AnswerStillAppliesUseCase,
	private val texts: AssistantChatFeedbackTexts
) {
	// [previous] is the thumbs the message had before this rating: a refused rating goes back to it.
	fun rate(host: MachineHost<AssistantChat.Effect>, messageId: String, useful: Boolean, previous: Boolean?) {
		host.launchMachineJob {
			val params = RateAssistantMessageParams(messageId = messageId, useful = useful)

			rateAssistantMessageUseCase.execute(params).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						AssistantChatInternalEvent.MessageRated(message = texts.rated())
					)

					is UseCaseState.Error -> host.processInternalEvent(
						AssistantChatInternalEvent.MessageRateFailed(
							messageId = messageId,
							rejected = useful,
							previous = previous,
							message = texts.rateFailed()
						)
					)
				}
			}
		}
	}

	fun report(
		host: MachineHost<AssistantChat.Effect>,
		messageId: String,
		reason: ReportReason,
		comment: String?
	) {
		host.launchMachineJob {
			val params = ReportAssistantMessageParams(messageId = messageId, reason = reason, comment = comment)

			reportAssistantMessageUseCase.execute(params).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						AssistantChatInternalEvent.MessageReported(messageId = messageId, message = texts.reported())
					)

					is UseCaseState.Error -> host.processInternalEvent(
						if (useCaseState.error == ReportAssistantMessageError.CommentTooLong) {
							AssistantChatInternalEvent.MessageReportRejected(
								message = texts.reportTooLong(AssistantInputLimits.REPORT_COMMENT_MAX_LENGTH)
							)
						} else {
							AssistantChatInternalEvent.MessageReportFailed(message = texts.reportFailed())
						}
					)
				}
			}
		}
	}

	// An answer that was not saved says so; the answer is not kept, so the card keeps asking.
	fun answerStillApplies(host: MachineHost<AssistantChat.Effect>, memoryId: String, applies: Boolean) {
		host.launchMachineJob {
			val params = AnswerStillAppliesParams(memoryId = memoryId, applies = applies)

			answerStillAppliesUseCase.execute(params).collect { useCaseState ->
				if (useCaseState is UseCaseState.Error) {
					host.processInternalEvent(
						AssistantChatInternalEvent.StillAppliesRejected(message = texts.stillAppliesFailed())
					)
				}
			}
		}
	}
}
