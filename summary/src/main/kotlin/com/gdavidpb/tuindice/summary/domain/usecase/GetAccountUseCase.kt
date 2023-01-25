package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ConcurrencyRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.base.utils.Locks
import com.gdavidpb.tuindice.base.utils.extension.*
import com.gdavidpb.tuindice.summary.domain.error.GetAccountError
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository

class GetAccountUseCase(
	private val authRepository: AuthRepository,
	private val accountRepository: AccountRepository,
	private val concurrencyRepository: ConcurrencyRepository,
	private val networkRepository: NetworkRepository
) : ResultUseCase<Unit, Account, GetAccountError>() {
	override suspend fun executeOnBackground(params: Unit): Account {
		val activeUId = authRepository.getActiveAuth().uid

		return concurrencyRepository.withLock(name = Locks.GET_ACCOUNT_AND_QUARTERS) {
			accountRepository.getAccount(uid = activeUId)
		}
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