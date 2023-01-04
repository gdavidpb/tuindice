package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.PersistenceRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase

class GetSubjectUseCase(
	private val authRepository: AuthRepository,
	private val persistenceRepository: PersistenceRepository
) : ResultUseCase<String, Subject, Nothing>() {
	override suspend fun executeOnBackground(params: String): Subject {
		val activeUId = authRepository.getActiveAuth().uid

		return persistenceRepository.getSubject(uid = activeUId, sid = params)
	}
}