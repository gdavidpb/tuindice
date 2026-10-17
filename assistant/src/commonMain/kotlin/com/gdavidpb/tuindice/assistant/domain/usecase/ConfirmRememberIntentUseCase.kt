package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantIntentError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.AssistantIntentExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ConfirmIntentParams
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.RememberArgs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Accepting a memory only decides it: the server applies the memory, so the success is not reported back. */
class ConfirmRememberIntentUseCase(
	private val confirmer: AssistantIntentConfirmer,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: AssistantIntentExceptionHandler
) : FlowUseCase<ConfirmIntentParams, OutcomeKind, AssistantIntentError>() {
	override suspend fun executeOnBackground(params: ConfirmIntentParams): Flow<OutcomeKind> = flow {
		emit(
			confirmer.confirm(
				params,
				narrow = { it as? RememberArgs },
				reportsOutcome = false
			) { _, _ ->
				IntentExecution(outcome = OutcomeKind.CONFIRMED)
			}
		)
	}
}
