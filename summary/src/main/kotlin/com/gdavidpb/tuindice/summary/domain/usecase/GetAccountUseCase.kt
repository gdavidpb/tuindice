package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository

class GetAccountUseCase(
	private val authRepository: AuthRepository,
	private val accountRepository: AccountRepository
) : ResultUseCase<Unit, Account, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Account {
		val activeUId = authRepository.getActiveAuth().uid

		return accountRepository.getAccount(uid = activeUId)
	}
}