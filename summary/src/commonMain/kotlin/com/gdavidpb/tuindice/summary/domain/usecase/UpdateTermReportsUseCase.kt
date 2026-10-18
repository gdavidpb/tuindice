package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.attempt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Asks the service for the term reports again and keeps the ones it answers on this device. It emits whether the
 * service answered. A failure is reported and keeps the reports already read: the screens say what they can without
 * them.
 */
class UpdateTermReportsUseCase(
	private val insightsRepository: AssistantInsightsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Boolean, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Boolean> = flow {
		val isRefreshed = attempt { insightsRepository.refreshInsights() }
			.onFailure { failure -> reportHandledException(failure) }
			.isSuccess

		emit(isRefreshed)
	}
}
