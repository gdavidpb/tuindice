package com.gdavidpb.tuindice.data.utils

import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.utils.extension.getAttestation
import com.gdavidpb.tuindice.base.utils.extension.isAttestedApi
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import kotlin.reflect.full.createInstance

class AttestationInterceptor(
	private val attestationRepository: AttestationRepository
) : Interceptor {
	override fun intercept(chain: Interceptor.Chain): Response {
		val request = chain.request()
		val isAttestedApi = request.isAttestedApi()

		return if (isAttestedApi) {
			val attestedRequest = request
				.newBuilder()
				.apply {
					val attestation = request.getAttestation()

					if (attestation != null) {
						val attestationToken = runCatching {
							val parser = attestation.parser.createInstance()
							val payload = parser.getPayload(request)

							runBlocking {
								attestationRepository.getToken(
									operation = request.url.encodedPath,
									payload = payload
								)
							}
						}.getOrNull()

						val hasAttestationToken = (attestationToken != null)

						if (hasAttestationToken) header("Attestation", "$attestationToken")
					}
				}
				.build()

			chain.proceed(attestedRequest)
		} else {
			chain.proceed(request)
		}
	}
}