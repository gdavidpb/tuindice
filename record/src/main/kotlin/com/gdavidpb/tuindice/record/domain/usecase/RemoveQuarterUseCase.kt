package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.mapper.toQuarterRemove
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.param.RemoveQuarterParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RemoveQuarterUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository
) : FlowUseCase<RemoveQuarterParams, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: RemoveQuarterParams): Flow<Unit> {
		val activeUId = authRepository.getActiveAuth().uid

		val remove = params.toQuarterRemove()

		quarterRepository.removeQuarter(uid = activeUId, remove = remove)

		return flowOf(Unit)
	}
}