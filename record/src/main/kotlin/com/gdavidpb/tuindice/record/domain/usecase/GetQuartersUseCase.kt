package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.GetQuartersExceptionHandler
import kotlinx.coroutines.flow.Flow

class GetQuartersUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository,
	override val exceptionHandler: GetQuartersExceptionHandler
) : FlowUseCase<Unit, List<Quarter>, GetQuartersError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<List<Quarter>> {
		val activeUId = authRepository.getActiveAuth().uid

		return quarterRepository.getQuarters(uid = activeUId)
	}
}