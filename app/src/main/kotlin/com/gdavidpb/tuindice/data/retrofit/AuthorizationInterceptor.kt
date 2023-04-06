package com.gdavidpb.tuindice.data.retrofit

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.login.utils.extension.isPublicApi
import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(
	private val settingsRepository: SettingsRepository
) : Interceptor {
	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()

		val authRequest = request
			.newBuilder()
			.header("App-Version", "${BuildConfig.VERSION_CODE}")
			.apply {
				val isPublicApi = request.isPublicApi()

				if (!isPublicApi) {
					val activeToken = settingsRepository.getActiveToken()
					val hasActiveToken = (activeToken != null)

					if (hasActiveToken) header("Authorization", "Bearer $activeToken")
				}
			}
			.build()

		return chain.proceed(authRequest)
	}
}