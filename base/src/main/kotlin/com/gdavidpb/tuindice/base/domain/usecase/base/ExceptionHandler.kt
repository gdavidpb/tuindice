package com.gdavidpb.tuindice.base.domain.usecase.base

interface ExceptionHandler<T> {
	fun parseException(throwable: Throwable): T?
}