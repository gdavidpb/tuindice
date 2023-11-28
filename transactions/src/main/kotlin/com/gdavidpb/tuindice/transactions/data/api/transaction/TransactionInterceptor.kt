package com.gdavidpb.tuindice.transactions.data.api.transaction

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.transactions.domain.repository.TransactionRepository
import com.gdavidpb.tuindice.transactions.utils.extension.getEnqueued
import com.gdavidpb.tuindice.transactions.utils.extension.isEnqueuedApi
import okhttp3.Interceptor
import okhttp3.Response
import kotlin.reflect.full.createInstance

class TransactionInterceptor(
	private val authRepository: AuthRepository,
	private val transactionRepository: TransactionRepository
) : Interceptor {
	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()
		val isEnqueuedApi = request.isEnqueuedApi()

		return if (isEnqueuedApi) {
			runCatching {
				chain.proceed(request)
			}.getOrElse { throwable ->
				val enqueue = request.getEnqueued()

				if (enqueue != null) {
					noAwait {
						val activeUId = authRepository.getActiveAuth().uid
						val transactionParser = enqueue.parser.createInstance()
						val transaction = transactionParser.parse(request)

						transactionRepository.enqueueTransaction(
							uid = activeUId,
							transaction = transaction
						)
					}
				}

				throw throwable
			}
		} else {
			chain.proceed(request)
		}
	}
}