package com.gdavidpb.tuindice.data.retrofit

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(
	private val settingsRepository: SettingsRepository
) : Interceptor {
	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()

		val bearerToken = settingsRepository.getActiveToken()

		val authRequest = request
			.newBuilder()
			.header("App-Version", "${BuildConfig.VERSION_CODE}")
			.apply {
				val isBearerTokenAvailable = (bearerToken != null)
				val isAuthorizationHeaderPresent = (request.headers["Authorization"] != null)

				if (isBearerTokenAvailable && !isAuthorizationHeaderPresent)
					header("Authorization", "Bearer $bearerToken")
			}
			.build()

		return chain.proceed(authRequest)
	}
}