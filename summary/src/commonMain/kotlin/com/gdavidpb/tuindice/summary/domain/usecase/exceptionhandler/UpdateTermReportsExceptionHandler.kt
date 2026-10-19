package com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateTermReportsUseCaseError

class UpdateTermReportsExceptionHandler : ExceptionHandler<UpdateTermReportsUseCaseError> {
	override fun parseException(throwable: Throwable): UpdateTermReportsUseCaseError? {
		return when {
			throwable.isConnection() -> UpdateTermReportsUseCaseError.NoConnection
			else -> null
		}
	}
}
