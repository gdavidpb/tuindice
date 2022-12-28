package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.TuIndiceRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase

class GetQuartersUseCase(
	private val authRepository: AuthRepository,
	private val tuIndiceRepository: TuIndiceRepository
) : ResultUseCase<Unit, List<Quarter>, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): List<Quarter> {
		val activeUId = authRepository.getActiveAuth().uid

		return tuIndiceRepository.getQuarters(uid = activeUId)
	}
}