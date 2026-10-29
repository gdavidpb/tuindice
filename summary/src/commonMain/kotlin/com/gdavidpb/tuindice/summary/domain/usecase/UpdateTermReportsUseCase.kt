package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateTermReportsUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateTermReportsExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Asks the service for the term reports again and keeps the ones it answers on this device. It emits once the service
 * has answered; when it has not, the use case fails with [UpdateTermReportsUseCaseError] and the reports already read
 * stay: the screens say what they can without them.
 */
class UpdateTermReportsUseCase(
	private val insightsRepository: AssistantInsightsRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: UpdateTermReportsExceptionHandler
) : FlowUseCase<Unit, Unit, UpdateTermReportsUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> = flow {
		insightsRepository.refreshInsights()

		emit(Unit)
	}
}
