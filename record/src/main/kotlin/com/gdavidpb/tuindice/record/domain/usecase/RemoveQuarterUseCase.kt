package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.PersistenceRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.CompletableUseCase

class RemoveQuarterUseCase(
	private val authRepository: AuthRepository,
	private val persistenceRepository: PersistenceRepository
) : CompletableUseCase<String, Nothing>() {
	override suspend fun executeOnBackground(params: String) {
		val activeUId = authRepository.getActiveAuth().uid

		persistenceRepository.removeQuarter(uid = activeUId, qid = params)
	}
}