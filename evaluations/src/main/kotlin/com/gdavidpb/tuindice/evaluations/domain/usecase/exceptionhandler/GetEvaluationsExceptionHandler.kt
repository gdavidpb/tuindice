package com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.exception.NoSubjectsException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsError

class GetEvaluationsExceptionHandler(
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<EvaluationsError>() {
	override fun parseException(throwable: Throwable): EvaluationsError? {
		return when (throwable) {
			is NoSubjectsException -> EvaluationsError.NoSubjects
			else -> null
		}
	}
}