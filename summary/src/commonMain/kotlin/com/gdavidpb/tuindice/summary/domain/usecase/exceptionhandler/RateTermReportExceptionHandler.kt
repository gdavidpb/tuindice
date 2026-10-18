package com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.base.utils.extension.isUnauthorized
import com.gdavidpb.tuindice.summary.domain.usecase.error.RateTermReportUseCaseError

class RateTermReportExceptionHandler : ExceptionHandler<RateTermReportUseCaseError> {
	override fun parseException(throwable: Throwable): RateTermReportUseCaseError? {
		return when {
			throwable.isUnauthorized() || throwable.isNotFound() -> RateTermReportUseCaseError.Refused
			throwable.isConnection() -> RateTermReportUseCaseError.NoConnection
			else -> null
		}
	}
}
