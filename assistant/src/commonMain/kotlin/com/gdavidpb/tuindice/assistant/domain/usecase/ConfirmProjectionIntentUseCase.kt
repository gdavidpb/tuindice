package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantProjectionRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.error.AssistantIntentError
import com.gdavidpb.tuindice.assistant.domain.usecase.exceptionhandler.AssistantIntentExceptionHandler
import com.gdavidpb.tuindice.assistant.domain.usecase.param.ConfirmIntentParams
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import com.gdavidpb.tuindice.chatcontract.intent.ProjectionArgs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ConfirmProjectionIntentUseCase(
	private val confirmer: AssistantIntentConfirmer,
	private val projectionRepository: AssistantProjectionRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: AssistantIntentExceptionHandler
) : FlowUseCase<ConfirmIntentParams, OutcomeKind, AssistantIntentError>() {
	override suspend fun executeOnBackground(params: ConfirmIntentParams): Flow<OutcomeKind> = flow {
		emit(
			confirmer.confirm(params, narrow = { it as? ProjectionArgs }) { args, intent ->
				projectionRepository.save(args, expectedRevision = intent.binding.expectedRevision)
			}
		)
	}
}
