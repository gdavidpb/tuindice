package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetUserUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.GetUserExceptionHandler
import kotlinx.coroutines.flow.Flow

class GetUserUseCase(
	private val userRepository: UserRepository,
	override val exceptionHandler: GetUserExceptionHandler
) : FlowUseCase<Unit, User, GetUserUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<User> {
		return userRepository.getUserFlow()
	}
}