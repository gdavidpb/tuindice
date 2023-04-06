package com.gdavidpb.tuindice.transactions.data.retrofit

import com.gdavidpb.tuindice.transactions.domain.repository.TransactionRepository
import okhttp3.Interceptor
import okhttp3.Response

class OfflineInterceptor(
	private val transactionRepository: TransactionRepository
) : Interceptor {
	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()

		return runCatching {
			chain.proceed(request)
		}.getOrElse { throwable ->
			// TODO transactionRepository.enqueueTransaction(null)

			throw throwable
		}
	}
}