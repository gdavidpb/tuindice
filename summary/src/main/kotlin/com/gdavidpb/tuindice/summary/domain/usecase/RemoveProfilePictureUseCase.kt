package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RemoveProfilePictureUseCase(
	private val authRepository: AuthRepository,
	private val accountRepository: AccountRepository,
	override val exceptionHandler: RemoveProfilePictureExceptionHandler
) : FlowUseCase<Unit, Unit, ProfilePictureError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		val activeUId = authRepository.getActiveAuth().uid

		accountRepository.removeProfilePicture(uid = activeUId)

		return flowOf(Unit)
	}
}