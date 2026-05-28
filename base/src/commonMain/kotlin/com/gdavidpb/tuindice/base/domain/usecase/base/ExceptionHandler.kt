package com.gdavidpb.tuindice.base.domain.usecase.base

abstract class ExceptionHandler<T : UseCaseError> {
	open fun parseException(throwable: Throwable): T? = null
}
