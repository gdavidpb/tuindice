package com.gdavidpb.tuindice.data.retrofit

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.utils.extension.headerPutIfAbsent
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