package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

abstract class FlowUseCase<P, T, E : UseCaseError>(
	protected open val backgroundDispatcher: CoroutineDispatcher = Dispatchers.Default,
	protected open val reportingRepository: ReportingRepository
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
				if (throwable is CancellationException) throw throwable

				val error = exceptionHandler?.parseException(throwable)

				val useCase = this@FlowUseCase::class.simpleName ?: "Unknown"
				val isHandled = error != null

				reportingRepository.setCustomKey("use-case", useCase)
				reportingRepository.setCustomKey("is-handled", isHandled)
				reportingRepository.logException(throwable)

				emit(UseCaseState.Error(error))
			}
	}
}
