package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Sends again the outcome reports the server has not taken yet, so a report whose send failed is not lost.
class FlushAssistantOutcomesUseCase(
	private val confirmer: AssistantIntentConfirmer,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> = flow {
		confirmer.flushPendingOutcomes()

		emit(Unit)
	}
}
