package com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.exception.EvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationError

class UpdateEvaluationExceptionHandler : ExceptionHandler<EvaluationError> {
	override fun parseException(throwable: Throwable): EvaluationError? {
		return when (throwable) {
			is EvaluationIllegalArgumentException -> throwable.error
			else -> null
		}
	}
}