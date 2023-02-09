package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.baseV2.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.summary.domain.error.ProfilePictureError
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RemoveProfilePictureUseCase(
	private val authRepository: AuthRepository,
	private val networkRepository: NetworkRepository,
	private val accountRepository: AccountRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Unit, ProfilePictureError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Unit> {
		val activeUId = authRepository.getActiveAuth().uid

		accountRepository.removeProfilePicture(uid = activeUId)

		return flowOf(Unit)
	}

	override suspend fun executeOnException(throwable: Throwable): ProfilePictureError? {
		return when {
			throwable.isTimeout() -> ProfilePictureError.Timeout
			throwable.isConnection() -> ProfilePictureError.NoConnection(networkRepository.isAvailable())
			else -> null
		}
	}
}