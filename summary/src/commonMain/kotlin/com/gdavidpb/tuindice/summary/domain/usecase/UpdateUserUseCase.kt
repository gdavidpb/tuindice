package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateUserUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateUserUseCase(
	private val userRepository: UserRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: UpdateUserExceptionHandler,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : FlowUseCase<Unit, Unit, UpdateUserUseCaseError>(
	reportingRepository = reportingRepository,
	dispatchers = dispatchers
) {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		userRepository.updateUser()

		return flowOf(Unit)
	}
}
