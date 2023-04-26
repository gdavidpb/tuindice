package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Account
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.summary.domain.repository.AccountRepository
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetAccountError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.GetAccountExceptionHandler
import kotlinx.coroutines.flow.Flow

class GetAccountUseCase(
	private val authRepository: AuthRepository,
	private val accountRepository: AccountRepository,
	override val exceptionHandler: GetAccountExceptionHandler
) : FlowUseCase<Unit, Account, GetAccountError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Account> {
		val activeUId = authRepository.getActiveAuth().uid

		return accountRepository.getAccount(uid = activeUId)
	}
}