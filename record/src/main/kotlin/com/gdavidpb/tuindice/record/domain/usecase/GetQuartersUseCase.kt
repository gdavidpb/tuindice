package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository

class GetQuartersUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository
) : ResultUseCase<Unit, List<Quarter>, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): List<Quarter> {
		val activeUId = authRepository.getActiveAuth().uid

		// TODO define forceRemote from isUpdated
		return quarterRepository.getQuarters(uid = activeUId, forceRemote = true)
	}
}