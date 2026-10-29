package com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isAccessRejected
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateTermReportsUseCaseError

class UpdateTermReportsExceptionHandler : ExceptionHandler<UpdateTermReportsUseCaseError> {
	override fun parseException(throwable: Throwable): UpdateTermReportsUseCaseError? {
		return when {
			// The refusals that end the session (see SessionRecoveryDataSource), read from the status before the message.
			throwable.isAccessRejected() -> UpdateTermReportsUseCaseError.SessionEnded
			throwable.isConnection() -> UpdateTermReportsUseCaseError.NoConnection
			else -> null
		}
	}
}
