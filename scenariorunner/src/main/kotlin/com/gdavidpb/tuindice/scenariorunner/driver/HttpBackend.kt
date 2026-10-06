package com.gdavidpb.tuindice.scenariorunner.driver

import com.gdavidpb.tuindice.scenariokit.driver.BackendControl
import com.gdavidpb.tuindice.scenariokit.driver.HttpReply
import java.net.HttpURLConnection
import java.net.URL

/** Plain HTTP against the WireMock at [baseUrl]; a transport failure answers status -1. */
internal class HttpBackend(private val baseUrl: String) : BackendControl {
	override fun http(method: String, path: String, body: String?, authorization: String?): HttpReply =
		runCatching { exchange(method, path, body, authorization) }
			.getOrElse { HttpReply(TRANSPORT_FAILURE, it.toString()) }

	private fun exchange(method: String, path: String, body: String?, authorization: String?): HttpReply {
		val connection = URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection

		try {
			connection.requestMethod = method
			connection.connectTimeout = CONNECT_TIMEOUT_MS
			connection.readTimeout = READ_TIMEOUT_MS
			authorization?.let { connection.setRequestProperty("Authorization", it) }

			if (body != null) {
				connection.doOutput = true
				connection.setRequestProperty("Content-Type", "application/json")
				connection.outputStream.use { it.write(body.toByteArray()) }
			}

			val status = connection.responseCode
			val stream = if (status >= HTTP_ERROR) connection.errorStream else connection.inputStream

			return HttpReply(status, stream?.bufferedReader()?.use { it.readText() }.orEmpty())
		} finally {
			connection.disconnect()
		}
	}

	private companion object {
		const val TRANSPORT_FAILURE = -1
		const val HTTP_ERROR = 400
		const val CONNECT_TIMEOUT_MS = 3_000
		const val READ_TIMEOUT_MS = 5_000
	}
}
