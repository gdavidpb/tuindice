package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.utils.extension.*
import com.gdavidpb.tuindice.record.domain.error.GetQuartersError
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow

class GetQuartersUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository,
	private val networkRepository: NetworkRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, List<Quarter>, GetQuartersError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<List<Quarter>> {
		val activeUId = authRepository.getActiveAuth().uid

		return quarterRepository.getQuarters(uid = activeUId)
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