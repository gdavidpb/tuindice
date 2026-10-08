package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.domain.exception.ServiceRetryWindowException
import com.gdavidpb.tuindice.base.domain.exception.SessionRecoveryAttestationException
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ktor.serverResponseException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExceptionTest {
	private fun responseWith(code: Int, message: String = "status $code"): Throwable {
		val status = HttpStatusCode.fromValue(code)

		return if (code >= 500) {
			serverResponseException(status, message = message)
		} else {
			clientRequestException(status, message = message)
		}
	}

	@Test
	fun isRetryableLater_isTrueForTheStatusesThatDoNotSpeakAboutTheRequest_whateverTheMessageSays() {
		listOf(426, 429, 502, 503, 504).forEach { code ->
			assertTrue(responseWith(code).isRetryableLater(), "status $code")
		}
	}

	@Test
	fun isRetryableLater_isFalseForTheStatusesThatAreAVerdict() {
		listOf(400, 401, 403, 404, 408, 409, 412, 422, 423, 500, 501).forEach { code ->
			assertFalse(responseWith(code).isRetryableLater(), "status $code")
		}
	}

	@Test
	fun isRetryableLater_isTrueForTheRetryWindow() {
		assertTrue(ServiceRetryWindowException(retryAfterMillis = 1_000L).isRetryableLater())
	}

	@Test
	fun isRetryableLater_isTrueForAnAttestationRefusedDuringRecovery_alsoAsACause() {
		val attestation = SessionRecoveryAttestationException(responseWith(403))

		assertTrue(attestation.isRetryableLater())
		assertTrue(IllegalStateException("refresh failed", attestation).isRetryableLater())
	}

	@Test
	fun isRetryableLater_decidesByStatus_notByTheTimeoutWordInTheMessage() {
		assertTrue(responseWith(504, message = "upstream did not answer").isRetryableLater())
		assertFalse(responseWith(500, message = "request timeout inside the handler").isRetryableLater())
		assertFalse(responseWith(408, message = "request took too long").isRetryableLater())
		assertFalse(IllegalStateException("timeout").isRetryableLater())
	}
}
