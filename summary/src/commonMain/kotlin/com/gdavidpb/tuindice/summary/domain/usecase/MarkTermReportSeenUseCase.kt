package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.TermReportSeenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Opening a report marks it as seen, so the summary stops offering it.
class MarkTermReportSeenUseCase(
	private val seenRepository: TermReportSeenRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> = flow {
		seenRepository.markTermReportSeen(insightId = params)

		emit(Unit)
	}
}
