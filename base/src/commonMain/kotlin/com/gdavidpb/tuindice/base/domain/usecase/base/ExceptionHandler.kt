package com.gdavidpb.tuindice.base.domain.usecase.base

interface ExceptionHandler<T : UseCaseError> {
	fun parseException(throwable: Throwable): T?
}
