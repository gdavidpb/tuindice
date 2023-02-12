package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.*
import com.gdavidpb.tuindice.summary.domain.error.GetAccountError
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow

class GetAccountUseCase(
	private val authRepository: AuthRepository,
	private val accountRepository: AccountRepository,
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Account, GetAccountError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Account> {
		val activeUId = authRepository.getActiveAuth().uid

		return accountRepository.getAccount(uid = activeUId)
	}

	override suspend fun executeOnException(throwable: Throwable): GetAccountError? {
		return when {
			throwable.isForbidden() -> GetAccountError.AccountDisabled
			throwable.isUnavailable() -> GetAccountError.Unavailable
			throwable.isConflict() -> GetAccountError.OutdatedPassword
			throwable.isTimeout() -> GetAccountError.Timeout
			throwable.isConnection() -> GetAccountError.NoConnection(networkRepository.isAvailable())
			else -> super.executeOnException(throwable)
		}
	}
}