package com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.error.ShareTermImageUseCaseError

// To the student every failure of the share reads the same: the picture could not be prepared.
class ShareTermImageExceptionHandler : ExceptionHandler<ShareTermImageUseCaseError> {
	override fun parseException(throwable: Throwable): ShareTermImageUseCaseError {
		return ShareTermImageUseCaseError.ImageUnavailable
	}
}
