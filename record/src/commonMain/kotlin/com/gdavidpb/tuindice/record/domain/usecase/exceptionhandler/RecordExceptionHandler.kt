package com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isConnection
import com.gdavidpb.tuindice.base.utils.extension.isTimeout
import com.gdavidpb.tuindice.base.utils.extension.isUnauthorized
import com.gdavidpb.tuindice.base.utils.extension.isUnavailable
import com.gdavidpb.tuindice.record.domain.exception.SyntheticTermValidationException
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError

class RecordExceptionHandler : ExceptionHandler<RecordUseCaseError>() {
	override fun parseException(throwable: Throwable): RecordUseCaseError? {
		return when {
			throwable is SyntheticTermValidationException ->
				RecordUseCaseError.SyntheticTermValidation(throwable.reason)
			throwable.isUnauthorized() -> RecordUseCaseError.Unauthorized
			throwable.isTimeout() -> RecordUseCaseError.Timeout
			throwable.isUnavailable() -> RecordUseCaseError.Unavailable
			throwable.isConnection() -> RecordUseCaseError.NoConnection
			else -> null
		}
	}
}
