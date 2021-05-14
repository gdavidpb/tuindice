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
		val token = runBlocking { authRepository.getToken(forceRefresh = false) }

		return if (token != null) {
			val authRequest = request
				.newBuilder()
				.header("Authorization", "Bearer $token")
				.build()

			chain.proceed(authRequest)
		} else {
			chain.proceed(request)
		}
	}
}