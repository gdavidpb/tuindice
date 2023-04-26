package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.mapper.toSubjectUpdate
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.param.WithdrawSubjectParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class WithdrawSubjectUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository
) : FlowUseCase<WithdrawSubjectParams, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: WithdrawSubjectParams): Flow<Unit> {
		val activeUId = authRepository.getActiveAuth().uid

		val update = params.toSubjectUpdate()

		quarterRepository.updateSubject(uid = activeUId, update = update)

		return flowOf(Unit)
	}
}