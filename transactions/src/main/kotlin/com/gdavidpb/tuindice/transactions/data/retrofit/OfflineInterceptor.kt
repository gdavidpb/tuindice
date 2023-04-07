package com.gdavidpb.tuindice.transactions.data.retrofit

import com.gdavidpb.tuindice.transactions.domain.repository.TransactionRepository
import com.gdavidpb.tuindice.transactions.utils.extension.isEnqueueOnFailure
import okhttp3.Interceptor
import okhttp3.Response

class OfflineInterceptor(
	private val transactionRepository: TransactionRepository
) : Interceptor {
	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()
		val isEnqueueOnFailure = request.isEnqueueOnFailure()

		return if (isEnqueueOnFailure) {
			runCatching {
				chain.proceed(request)
			}.getOrElse { throwable ->
				// TODO transactionRepository.enqueueTransaction(null)

				throw throwable
			}
		} else {
			chain.proceed(request)
		}
	}
}