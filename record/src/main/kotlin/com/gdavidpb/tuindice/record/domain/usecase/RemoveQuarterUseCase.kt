package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.baseV2.FlowUseCase
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RemoveQuarterUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		val activeUId = authRepository.getActiveAuth().uid

		quarterRepository.removeQuarter(uid = activeUId, qid = params)

		return flowOf(Unit)
	}
}