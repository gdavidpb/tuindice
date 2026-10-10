package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow

/**
 * The server ending the session behind the user's back, told as it happens. The latest
 * invalidation is kept until someone observes, so one that arrives while nobody is listening is
 * still delivered, once.
 */
class ObserveSessionInvalidationUseCase(
	private val sessionInvalidationRepository: SessionInvalidationRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		return sessionInvalidationRepository.observeSessionInvalidation()
	}
}
