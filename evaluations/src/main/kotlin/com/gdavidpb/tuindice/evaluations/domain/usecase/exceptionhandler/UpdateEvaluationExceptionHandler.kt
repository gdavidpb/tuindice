package com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.exception.UpdateEvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationError

class UpdateEvaluationExceptionHandler(
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<UpdateEvaluationError>() {
	override fun parseException(throwable: Throwable): UpdateEvaluationError? {
		return when (throwable) {
			is UpdateEvaluationIllegalArgumentException -> throwable.error
			else -> null
		}
	}
}