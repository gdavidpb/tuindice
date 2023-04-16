package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.utils.ReportKeys
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

abstract class FlowUseCase<P, T, E>(
	protected open val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
	protected abstract val reportingRepository: ReportingRepository

	protected open val paramsValidator: ParamsValidator<P>? = null

	protected open val exceptionHandler: ExceptionHandler<E>? = null

	abstract suspend fun executeOnBackground(params: P): Flow<T>

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
				val error = exceptionHandler?.parseException(throwable)

				logException(throwable, error)

				emit(UseCaseState.Error(error))
			}
	}

	private fun logException(throwable: Throwable, error: E?) {
		with(reportingRepository) {
			setCustomKey(ReportKeys.USE_CASE, "${this::class.simpleName}")
			setCustomKey(ReportKeys.IS_HANDLED, error != null)
			logException(throwable)
		}
	}
}