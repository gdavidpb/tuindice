package com.gdavidpb.tuindice.base.domain.usecase.baseV2

import com.gdavidpb.tuindice.base.domain.validator.Validator

abstract class BaseUseCase<P, T, E> {
	protected open val paramsValidator: Validator<P>? = null

	abstract suspend fun executeOnBackground(params: P): T

	protected open suspend fun executeOnException(throwable: Throwable): E? = null
}