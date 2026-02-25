package com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError

class SetSubjectGradeExceptionHandler(
	override val reportingRepository: ReportingGateway
) : ExceptionHandler<SubjectUseCaseError>() {
	override fun parseException(throwable: Throwable): SubjectUseCaseError? {
		return when (throwable) {
			is SubjectIllegalArgumentException -> throwable.error
			else -> null
		}
	}
}
