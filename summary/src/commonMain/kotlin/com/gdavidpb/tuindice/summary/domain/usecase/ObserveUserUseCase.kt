package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.model.ObservedSummary
import com.gdavidpb.tuindice.summary.domain.repository.CurrentTermRepository
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.ObserveUserUseCaseError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveUserUseCase(
	private val userRepository: UserRepository,
	private val currentTermRepository: CurrentTermRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, ObservedSummary, ObserveUserUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<ObservedSummary> {
		// The current term is folded in here so the state arrives carrying it: the route only
		// bridges state and effects, it does not read the record on its own.
		return combine(
			userRepository.observeUserFlow(),
			currentTermRepository.observeHasCurrentTerm()
		) { user, hasCurrentTerm ->
			ObservedSummary(
				user = user,
				hasCurrentTerm = hasCurrentTerm
			)
		}
	}
}
