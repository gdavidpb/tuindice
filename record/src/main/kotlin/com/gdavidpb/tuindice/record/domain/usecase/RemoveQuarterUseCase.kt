package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.CompletableUseCase
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository

class RemoveQuarterUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository
) : CompletableUseCase<String, Nothing>() {
	override suspend fun executeOnBackground(params: String) {
		val activeUId = authRepository.getActiveAuth().uid

		quarterRepository.removeQuarter(uid = activeUId, qid = params)
	}
}