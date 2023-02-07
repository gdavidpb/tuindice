package com.gdavidpb.tuindice.base.domain.usecase.baseV2

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

abstract class FlowUseCase<P, T, E>(
	protected open val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseUseCase<P, Flow<T>, E>() {
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
}