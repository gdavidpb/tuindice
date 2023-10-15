package com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.exception.AddEvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationError

class AddEvaluationExceptionHandler(
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<AddEvaluationError>() {
	override fun parseException(throwable: Throwable): AddEvaluationError? {
		return when (throwable) {
			is AddEvaluationIllegalArgumentException -> throwable.error
			else -> null
		}
	}
}