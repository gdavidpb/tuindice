package com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.exception.AddEvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError

class AddEvaluationExceptionHandler(
	override val reportingRepository: ReportingGateway
) : ExceptionHandler<AddEvaluationUseCaseError>() {
	override fun parseException(throwable: Throwable): AddEvaluationUseCaseError? {
		return when (throwable) {
			is AddEvaluationIllegalArgumentException -> throwable.error
			else -> null
		}
	}
}
