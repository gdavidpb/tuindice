package com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectError

class UpdateSubjectExceptionHandler : ExceptionHandler<SubjectError> {
	override fun parseException(throwable: Throwable): SubjectError? {
		return when (throwable) {
			is SubjectIllegalArgumentException -> throwable.error
			else -> null
		}
	}
}