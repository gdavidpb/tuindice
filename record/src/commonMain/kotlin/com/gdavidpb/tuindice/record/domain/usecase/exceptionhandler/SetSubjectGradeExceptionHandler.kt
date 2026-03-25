package com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isPreconditionFailed
import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError

class SetSubjectGradeExceptionHandler(
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<SubjectUseCaseError>() {
		override fun parseException(throwable: Throwable): SubjectUseCaseError? {
			return when (throwable) {
				is SubjectIllegalArgumentException -> throwable.error
				else -> when {
					throwable.isNotFound() -> SubjectUseCaseError.NotFound
					throwable.isPreconditionFailed() -> SubjectUseCaseError.Conflict
					else -> null
				}
			}
		}
}
