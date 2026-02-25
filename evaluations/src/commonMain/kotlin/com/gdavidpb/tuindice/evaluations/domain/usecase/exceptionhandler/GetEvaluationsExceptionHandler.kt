package com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.exception.NoSubjectsException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError

class GetEvaluationsExceptionHandler(
	override val reportingRepository: ReportingGateway
) : ExceptionHandler<EvaluationsUseCaseError>() {
	override fun parseException(throwable: Throwable): EvaluationsUseCaseError? {
		return when (throwable) {
			is NoSubjectsException -> EvaluationsUseCaseError.NoSubjects
			else -> null
		}
	}
}
