package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.ObserveUserUseCaseError
import kotlinx.coroutines.flow.Flow

class ObserveUserUseCase(
	private val userRepository: UserRepository,
	override val reportingRepository: ReportingRepository,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : FlowUseCase<Unit, User, ObserveUserUseCaseError>(
	reportingRepository = reportingRepository,
	dispatchers = dispatchers
) {
	override suspend fun executeOnBackground(params: Unit): Flow<User> {
		return userRepository.observeUserFlow()
	}
}
