package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.CompletableUseCase
import com.gdavidpb.tuindice.record.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository

class WithdrawSubjectUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository
) : CompletableUseCase<String, Nothing>() {
	override suspend fun executeOnBackground(params: String) {
		val activeUId = authRepository.getActiveAuth().uid

		val update = SubjectUpdate(
			subjectId = params,
			grade = MIN_SUBJECT_GRADE,
			dispatchToRemote = true
		)

		quarterRepository.updateSubject(uid = activeUId, update = update)
	}
}