package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ResultUseCase
import com.gdavidpb.tuindice.record.domain.error.SubjectError
import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.mapper.toSubjectUpdate
import com.gdavidpb.tuindice.record.domain.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.validator.UpdateSubjectParamsValidator

class UpdateSubjectUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository,
	override val paramsValidator: UpdateSubjectParamsValidator
) : ResultUseCase<UpdateSubjectParams, Subject, SubjectError>() {
	override suspend fun executeOnBackground(params: UpdateSubjectParams): Subject {
		val activeUid = authRepository.getActiveAuth().uid

		return quarterRepository.updateSubject(
			uid = activeUid,
			update = params.toSubjectUpdate()
		)
	}

	override suspend fun executeOnException(throwable: Throwable): SubjectError? {
		return when (throwable) {
			is SubjectIllegalArgumentException -> throwable.error
			else -> super.executeOnException(throwable)
		}
	}
}