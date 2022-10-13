package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.DatabaseRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.base.utils.ComputationManager

class GetQuartersUseCase(
	private val authRepository: AuthRepository,
	private val databaseRepository: DatabaseRepository
) : ResultUseCase<Unit, List<Quarter>, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): List<Quarter> {
		val activeUId = authRepository.getActiveAuth().uid

		return databaseRepository.getQuarters(uid = activeUId).also { quarters ->
			ComputationManager.setQuarters(quarters)
		}
	}
}