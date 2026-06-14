package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RemoveProfilePictureUseCase(
	private val userRepository: UserRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: RemoveProfilePictureExceptionHandler
) : FlowUseCase<Unit, Unit, ProfilePictureUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		userRepository.removeProfilePicture()

		return flowOf(Unit)
	}
}
