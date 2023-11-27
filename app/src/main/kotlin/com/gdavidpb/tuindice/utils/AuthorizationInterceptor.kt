package com.gdavidpb.tuindice.utils

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.utils.extension.isPublicApi
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(
	private val authRepository: AuthRepository
) : Interceptor {
	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()

		val authRequest = request
			.newBuilder()
			.header("App-Version", "${BuildConfig.VERSION_CODE}")
			.apply {
				val isPublicApi = request.isPublicApi()

				if (!isPublicApi) {
					val activeToken = runCatching {
						runBlocking {
							authRepository.getActiveToken()
						}
					}.getOrNull()

					val hasActiveToken = (activeToken != null)

					if (hasActiveToken) header("Authorization", "Bearer $activeToken")
				}
			}
			.build()

		return chain.proceed(authRequest)
	}
}