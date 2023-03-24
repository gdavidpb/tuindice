package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectUpdateTransaction
import com.gdavidpb.tuindice.base.domain.model.transaction.Transaction
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.error.SubjectError
import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.validator.UpdateSubjectParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateSubjectUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository,
	override val reportingRepository: ReportingRepository,
	override val paramsValidator: UpdateSubjectParamsValidator
) : FlowUseCase<UpdateSubjectParams, Unit, SubjectError>() {
	override suspend fun executeOnBackground(params: UpdateSubjectParams): Flow<Unit> {
		val activeUid = authRepository.getActiveAuth().uid

		val operation = SubjectUpdateTransaction(
			subjectId = params.subjectId,
			grade = params.grade
		)

		val transaction = Transaction.Builder<SubjectUpdateTransaction>()
			.withReference(params.subjectId)
			.withDispatchToRemote(params.dispatchToRemote)
			.withOperation(operation)
			.build()

		quarterRepository.updateSubject(
			uid = activeUid,
			transaction = transaction
		)

		return flowOf(Unit)
	}

	override suspend fun executeOnException(throwable: Throwable): SubjectError? {
		return when (throwable) {
			is SubjectIllegalArgumentException -> throwable.error
			else -> super.executeOnException(throwable)
		}
	}
}