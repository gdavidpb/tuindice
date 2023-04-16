package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.utils.ReportKeys

abstract class ExceptionHandler<T> {
	protected abstract val reportingRepository: ReportingRepository

	protected open fun parseException(throwable: Throwable): T? = null

	fun reportException(throwable: Throwable): T? {
		val error = parseException(throwable)

		with(reportingRepository) {
			setCustomKey(ReportKeys.USE_CASE, "${this::class.simpleName}")
			setCustomKey(ReportKeys.IS_HANDLED, error != null)
			logException(throwable)
		}

		return error
	}
}