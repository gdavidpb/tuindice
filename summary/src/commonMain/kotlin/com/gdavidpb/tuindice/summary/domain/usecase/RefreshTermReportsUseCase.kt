package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.attempt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Reads the term reports again when the summary is opened or refreshed. A failure keeps the reports already read, and the
// summary is not told of it: the reports are an extra of the summary, not what it shows.
class RefreshTermReportsUseCase(
	private val insightsRepository: AssistantInsightsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> = flow {
		attempt { insightsRepository.refreshInsights() }
			.onFailure { failure -> reportHandledException(failure) }

		emit(Unit)
	}
}
