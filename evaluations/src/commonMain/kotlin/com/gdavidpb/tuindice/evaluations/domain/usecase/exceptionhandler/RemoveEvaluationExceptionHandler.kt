package com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.RemoveEvaluationUseCaseError

class RemoveEvaluationExceptionHandler : ExceptionHandler<RemoveEvaluationUseCaseError>() {
	override fun parseException(throwable: Throwable): RemoveEvaluationUseCaseError? {
		return when {
			throwable.isNotFound() -> RemoveEvaluationUseCaseError.NotFound
			else -> null
		}
	}
}
