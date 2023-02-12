package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.validator.Validator
import com.gdavidpb.tuindice.base.utils.ReportKeys
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

abstract class FlowUseCase<P, T, E>(
	protected open val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
	protected abstract val reportingRepository: ReportingRepository

	protected open val paramsValidator: Validator<P>? = null

	abstract suspend fun executeOnBackground(params: P): Flow<T>

	protected open suspend fun executeOnException(throwable: Throwable): E? = null

	fun execute(params: P): Flow<UseCaseState<T, E>> {
		return flow {
			emitAll(executeOnBackground(params))
		}.flowOn(backgroundDispatcher)
			.map { data ->
				UseCaseState.Data<T, E>(data) as UseCaseState<T, E>
			}
			.onStart {
				paramsValidator?.validate(params)

				emit(UseCaseState.Loading())
			}
			.catch { throwable ->
				val error = executeOnException(throwable)

				logException(throwable, error)

				emit(UseCaseState.Error(error))
			}
	}

	private fun logException(throwable: Throwable, error: E?) {
		with(reportingRepository) {
			setCustomKey(ReportKeys.USE_CASE, "${this::class.simpleName}")
			setCustomKey(ReportKeys.HANDLED, error != null)
			logException(throwable)
		}
	}
}