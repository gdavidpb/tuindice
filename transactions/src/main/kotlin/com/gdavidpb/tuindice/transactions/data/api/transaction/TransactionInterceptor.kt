package com.gdavidpb.tuindice.transactions.data.api.transaction

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.utils.extension.noAwait
import com.gdavidpb.tuindice.transactions.domain.repository.TransactionRepository
import com.gdavidpb.tuindice.transactions.utils.extension.isEnqueueOnFailure
import okhttp3.Interceptor
import okhttp3.Response

class TransactionInterceptor(
	private val authRepository: AuthRepository,
	private val transactionRepository: TransactionRepository,
	private val transactionParser: TransactionParser
) : Interceptor {
	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()
		val isEnqueueOnFailure = request.isEnqueueOnFailure()

		return if (isEnqueueOnFailure) {
			runCatching {
				chain.proceed(request)
			}.getOrElse { throwable ->
				noAwait {
					val activeUId = authRepository.getActiveAuth().uid
					val transaction = transactionParser.fromRequest(request)

					transactionRepository.enqueueTransaction(
						uid = activeUId,
						transaction = transaction
					)
				}

				throw throwable
			}
		} else {
			chain.proceed(request)
		}
	}
}