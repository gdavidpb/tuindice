package com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isPreconditionFailed
import com.gdavidpb.tuindice.evaluations.domain.exception.AddEvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError

class AddEvaluationExceptionHandler : ExceptionHandler<AddEvaluationUseCaseError> {
	override fun parseException(throwable: Throwable): AddEvaluationUseCaseError? {
		return when {
			throwable is AddEvaluationIllegalArgumentException -> throwable.error
			throwable.isPreconditionFailed() -> AddEvaluationUseCaseError.AlreadyExists
			else -> null
		}
	}
}
