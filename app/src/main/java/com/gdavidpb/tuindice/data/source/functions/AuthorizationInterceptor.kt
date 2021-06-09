package com.gdavidpb.tuindice.data.source.functions

import com.gdavidpb.tuindice.domain.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(
	private val authRepository: AuthRepository
) : Interceptor {
	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()

		val authRequest = runBlocking {
			val isActiveAuth = authRepository.isActiveAuth()

			if (isActiveAuth) {
				val bearerToken = authRepository.getActiveToken()

				request
					.newBuilder()
					.header("Authorization", "Bearer $bearerToken")
					.build()
			} else {
				request
			}
		}

		return chain.proceed(authRequest)
	}
}