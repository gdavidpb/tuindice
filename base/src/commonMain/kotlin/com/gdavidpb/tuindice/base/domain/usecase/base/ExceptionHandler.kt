package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository

abstract class ExceptionHandler<T : UseCaseError> {
	protected abstract val reportingRepository: ReportingRepository

	protected open fun parseException(throwable: Throwable): T? = null

	fun reportException(throwable: Throwable): T? {
		val error = parseException(throwable)
		val handlerName = this::class.simpleName.orEmpty()

		with(reportingRepository) {
			setCustomKey(USE_CASE_KEY, handlerName)
			setCustomKey(IS_HANDLED_KEY, error != null)
			logException(throwable)
		}

		return error
	}

	companion object {
		private const val USE_CASE_KEY = "useCase"
		private const val IS_HANDLED_KEY = "isHandled"
	}
}
