package com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.summary.domain.usecase.error.ObserveTermReportUseCaseError

class ObserveTermReportExceptionHandler : ExceptionHandler<ObserveTermReportUseCaseError> {
	override fun parseException(throwable: Throwable): ObserveTermReportUseCaseError? {
		return when {
			throwable.isConnection() -> ObserveTermReportUseCaseError.NoConnection
			else -> null
		}
	}
}
