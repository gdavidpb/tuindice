package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ConcurrencyRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.base.utils.Locks
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.record.domain.error.GetQuartersError
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository

class GetQuartersUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository,
	private val concurrencyRepository: ConcurrencyRepository,
	private val networkRepository: NetworkRepository
) : ResultUseCase<Unit, List<Quarter>, GetQuartersError>() {
	override suspend fun executeOnBackground(params: Unit): List<Quarter> {
		val activeUId = authRepository.getActiveAuth().uid

		return concurrencyRepository.withLock(name = Locks.GET_ACCOUNT_AND_QUARTERS) {
			quarterRepository.getQuarters(uid = activeUId)
		}
	}

	override suspend fun executeOnException(throwable: Throwable): GetQuartersError? {
		return when {
			throwable.isForbidden() -> GetQuartersError.AccountDisabled
			throwable.isUnavailable() -> GetQuartersError.Unavailable
			throwable.isConflict() -> GetQuartersError.OutdatedPassword
			throwable.isTimeout() -> GetQuartersError.Timeout
			throwable.isConnection() -> GetQuartersError.NoConnection(networkRepository.isAvailable())
			else -> super.executeOnException(throwable)
		}
	}
}