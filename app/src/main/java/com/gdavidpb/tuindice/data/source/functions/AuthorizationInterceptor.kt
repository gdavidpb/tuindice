package com.gdavidpb.tuindice.data.source.functions

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.utils.extensions.headerPutIfAbsent
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(
	private val authRepository: AuthRepository
) : Interceptor {
	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()

		val bearerToken = runBlocking {
			if (authRepository.isActiveAuth())
				authRepository.getActiveToken()
			else
				null
		}

		val authRequest = request
			.newBuilder()
			.header("App-Version", "${BuildConfig.VERSION_CODE}")
			.apply {
				if (bearerToken != null)
					headerPutIfAbsent(
						headers = request.headers,
						name = "Authorization",
						value = "Bearer $bearerToken"
					)
			}
			.build()

		return chain.proceed(authRequest)
	}
}