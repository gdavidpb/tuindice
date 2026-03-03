package com.gdavidpb.tuindice.base.domain.usecase.base

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

abstract class FlowUseCase<P, T, E : UseCaseError>(
	protected open val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
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
				emit(UseCaseState.Loading())
				paramsValidator?.validate(params)
			}
			.catch { throwable ->
				val error = exceptionHandler?.reportException(throwable)

				emit(UseCaseState.Error(error))
			}
	}
}
