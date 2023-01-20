package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ConcurrencyRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.base.utils.Locks
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository

class GetAccountUseCase(
	private val authRepository: AuthRepository,
	private val accountRepository: AccountRepository,
	private val concurrencyRepository: ConcurrencyRepository
) : ResultUseCase<Unit, Account, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Account {
		val activeUId = authRepository.getActiveAuth().uid

		return concurrencyRepository.withLock(name = Locks.GET_ACCOUNT_AND_QUARTERS) {
			accountRepository.getAccount(uid = activeUId)
		}
	}
}