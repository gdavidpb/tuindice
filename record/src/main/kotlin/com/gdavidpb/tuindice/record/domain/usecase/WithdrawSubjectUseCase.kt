package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.model.SubjectUpdate
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.utils.MIN_SUBJECT_GRADE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class WithdrawSubjectUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, Unit, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<Unit> {
		val activeUId = authRepository.getActiveAuth().uid

		val update = SubjectUpdate(
			subjectId = params,
			grade = MIN_SUBJECT_GRADE,
			dispatchToRemote = true
		)

		quarterRepository.updateSubject(uid = activeUId, update = update)

		return flowOf(Unit)
	}
}