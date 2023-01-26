package com.gdavidpb.tuindice.base.domain.usecase.base

import androidx.lifecycle.LiveData
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.validator.Validator
import com.gdavidpb.tuindice.base.utils.ReportKeys
import com.gdavidpb.tuindice.base.utils.extension.getTimeoutKey
import com.gdavidpb.tuindice.base.utils.extension.hasTimeoutKey
import kotlinx.coroutines.*

abstract class BaseUseCase<P, T, E, L : LiveData<*>>(
	protected open val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO,
	protected open val foregroundDispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
	protected abstract val configRepository: ConfigRepository
	protected abstract val reportingRepository: ReportingRepository

	protected open val paramsValidator: Validator<P>? = null

	abstract suspend fun executeOnBackground(params: P): T?

	open suspend fun executeOnException(throwable: Throwable): E? = null

	protected abstract suspend fun onStart(liveData: L)
	protected abstract suspend fun onEmpty(liveData: L)
	protected abstract suspend fun onSuccess(liveData: L, response: T)
	protected abstract suspend fun onError(liveData: L, error: E?)

	fun execute(params: P, liveData: L, coroutineScope: CoroutineScope) {
		coroutineScope.launch(foregroundDispatcher) {
			runCatching {
				paramsValidator?.validate(params)

				onStart(liveData)

				withContext(backgroundDispatcher) {
					if (hasTimeoutKey()) {
						val key = getTimeoutKey()
						val timeMillis = configRepository.getTimeout(key)

						withTimeout(timeMillis) { executeOnBackground(params) }
					} else {
						executeOnBackground(params)
					}
				}
			}.onSuccess { response ->
				if (response != null)
					onSuccess(liveData, response)
				else
					onEmpty(liveData)
			}.onFailure { throwable ->
				val error = runCatching { executeOnException(throwable) }.getOrNull()

				logException(throwable, error)

				onError(liveData, error)
			}
		}
	}

	private fun logException(throwable: Throwable, error: E?) {
		reportingRepository.setCustomKey(ReportKeys.USE_CASE, "${this::class.simpleName}")
		reportingRepository.setCustomKey(ReportKeys.HANDLED, error != null)
		reportingRepository.logException(throwable)
	}
}