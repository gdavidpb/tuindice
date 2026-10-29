package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantEvaluationCardRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.param.GetAssistantEvaluationCardsParams
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// The cards of the Evaluations tab for the evaluations a list of the chat names, read when they are asked for: the ones
// the device does not have are left out.
class GetAssistantEvaluationCardsUseCase(
	private val evaluationCardRepository: AssistantEvaluationCardRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<GetAssistantEvaluationCardsParams, Map<String, EvaluationCardItem>, Nothing>() {
	override suspend fun executeOnBackground(
		params: GetAssistantEvaluationCardsParams
	): Flow<Map<String, EvaluationCardItem>> {
		return flow { emit(evaluationCardRepository.evaluationCards(params.evaluationIds)) }
	}
}
