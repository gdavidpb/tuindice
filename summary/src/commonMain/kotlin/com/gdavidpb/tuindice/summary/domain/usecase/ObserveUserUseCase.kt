package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.ObserveUserUseCaseError
import kotlinx.coroutines.flow.Flow

class ObserveUserUseCase(
	private val userRepository: UserRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, User, ObserveUserUseCaseError>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: Unit): Flow<User> {
		return userRepository.observeUserFlow()
	}
}
