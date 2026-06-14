package com.gdavidpb.tuindice.base.domain.usecase.base

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.utils.reportingMessage
import com.gdavidpb.tuindice.base.domain.utils.reportingName
import com.gdavidpb.tuindice.base.domain.utils.rootCause
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

abstract class FlowUseCase<P, T, E : UseCaseError> {
	protected abstract val reportingRepository: ReportingRepository

	protected open val paramsValidator: ParamsValidator<P>? = null
	protected open val exceptionHandler: ExceptionHandler<E>? = null

	abstract suspend fun executeOnBackground(params: P): Flow<T>

	fun execute(params: P): Flow<UseCaseState<T, E>> {
		return flow<UseCaseState<T, E>> {
			emit(UseCaseState.Loading)
			paramsValidator?.validate(params)
			emitAll(executeOnBackground(params).map { data -> UseCaseState.Data(data) })
		}.catch { throwable ->
			if (throwable is CancellationException) throw throwable

			val error = UseCaseState.Error(reportException(throwable))

			emit(error)
		}
	}

	private fun reportException(throwable: Throwable): E? {
		val error = exceptionHandler?.parseException(throwable)
		val rootCause = throwable.rootCause()

		reportingRepository.setCustomKey("use-case", this::class.reportingName())
		reportingRepository.setCustomKey("is-handled", error != null)
		reportingRepository.setCustomKey("throwable-class", throwable.reportingName())
		reportingRepository.setCustomKey("throwable-message", throwable.reportingMessage())
		reportingRepository.setCustomKey("root-cause-class", rootCause.reportingName())
		reportingRepository.setCustomKey("root-cause-message", rootCause.reportingMessage())
		reportingRepository.setCustomKey("error-class", error.reportingName())
		reportingRepository.logException(throwable)

		return error
	}
}
