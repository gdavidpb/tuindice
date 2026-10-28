package com.gdavidpb.tuindice.assistant.presentation.machine.collaborator

import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmAddEvaluationsIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmDeleteEvaluationIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmEditEvaluationIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ConfirmRememberIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.DelegateEnrollmentProofIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.DismissAssistantIntentUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantIntentError
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ConfirmIntentParams
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantChatInternalEvent
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.RememberArgs
import com.gdavidpb.tuindice.chatcontract.intent.RemoveEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.RequestEnrollmentProofArgs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

/**
 * The intents about the student's own records and the enrollment proof, and the dismissal of any intent. The kinds of
 * the plans and the projection are confirmed by [AssistantChatPlanIntents].
 */
class AssistantChatIntents(
	private val dismissAssistantIntentUseCase: DismissAssistantIntentUseCase,
	private val delegateEnrollmentProofIntentUseCase: DelegateEnrollmentProofIntentUseCase,
	private val confirmRememberIntentUseCase: ConfirmRememberIntentUseCase,
	private val confirmAddEvaluationsIntentUseCase: ConfirmAddEvaluationsIntentUseCase,
	private val confirmEditEvaluationIntentUseCase: ConfirmEditEvaluationIntentUseCase,
	private val confirmDeleteEvaluationIntentUseCase: ConfirmDeleteEvaluationIntentUseCase
) {
	// A dismissal another run holds is not sent, and it is not a failure: only a dismissal that did not reach the service
	// says so.
	fun dismiss(host: MachineHost<AssistantChat.Effect>, intentId: String) {
		host.launchMachineJob {
			dismissAssistantIntentUseCase.execute(intentId).collect { useCaseState ->
				if (useCaseState is UseCaseState.Error) {
					host.processInternalEvent(AssistantChatInternalEvent.IntentDismissFailed)
				}
			}
		}
	}

	// The confirmation of an intent of these kinds, or null when the kind is another one.
	fun confirmation(
		args: IntentArgs,
		params: ConfirmIntentParams
	): Flow<UseCaseState<OutcomeKind, AssistantIntentError>>? {
		return when (args) {
			is RequestEnrollmentProofArgs -> delegateEnrollmentProofIntentUseCase.execute(params)
			is RememberArgs -> confirmRememberIntentUseCase.execute(params)
			is AddEvaluationsArgs -> confirmAddEvaluationsIntentUseCase.execute(params)
			is EditEvaluationArgs -> confirmEditEvaluationIntentUseCase.execute(params)
			is RemoveEvaluationArgs -> confirmDeleteEvaluationIntentUseCase.execute(params)
			else -> null
		}
	}
}
