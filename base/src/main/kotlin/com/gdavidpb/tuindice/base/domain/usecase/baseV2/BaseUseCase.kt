package com.gdavidpb.tuindice.base.domain.usecase.baseV2

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.validator.Validator
import com.gdavidpb.tuindice.base.utils.ReportKeys

abstract class BaseUseCase<P, T, E> {
	protected abstract val reportingRepository: ReportingRepository

	protected open val paramsValidator: Validator<P>? = null

	abstract suspend fun executeOnBackground(params: P): T

	protected open suspend fun executeOnException(throwable: Throwable): E? = null

	protected fun logException(throwable: Throwable, error: E?) {
		with(reportingRepository) {
			setCustomKey(ReportKeys.USE_CASE, "${this::class.simpleName}")
			setCustomKey(ReportKeys.HANDLED, error != null)
			logException(throwable)
		}
	}
}