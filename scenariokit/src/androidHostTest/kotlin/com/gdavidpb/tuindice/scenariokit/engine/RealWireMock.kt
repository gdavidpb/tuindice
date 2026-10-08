package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.BackendControl
import com.gdavidpb.tuindice.scenariokit.driver.HttpReply
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.ServerSocket
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * The WireMock the harness runs (`mocks/wiremock-standalone-3.13.2.jar`), started in a process of its own on a free
 * port, and spoken to over plain HTTP like the device does.
 */
class RealWireMock : BackendControl, AutoCloseable {
	private val port = ServerSocket(0).use { it.localPort }
	private val process: Process

	init {
		val jar = listOf("../mocks/wiremock-standalone-3.13.2.jar", "mocks/wiremock-standalone-3.13.2.jar")
			.map(::File).first { it.isFile }
		val java = File(System.getProperty("java.home"), "bin/java").path
		val root = File.createTempFile("wiremock", "").also { it.delete() }.also(File::mkdirs)
		process = ProcessBuilder(java, "-jar", jar.path, "--port", "$port", "--root-dir", root.path, "--disable-banner")
			.redirectErrorStream(true)
			.redirectOutput(File(root, "wiremock.log"))
			.start()
		val deadline = System.currentTimeMillis() + STARTUP_MS
		while (http("GET", "/__admin/mappings", null, null).status != HTTP_OK) {
			check(System.currentTimeMillis() < deadline) { "WireMock did not start within $STARTUP_MS ms" }
			Thread.sleep(POLL_MS)
		}
	}

	override fun http(method: String, path: String, body: String?, authorization: String?): HttpReply {
		val connection = URL("http://localhost:$port$path").openConnection() as HttpURLConnection
		return try {
			connection.requestMethod = method
			connection.connectTimeout = IO_MS
			connection.readTimeout = IO_MS
			authorization?.let { connection.setRequestProperty("Authorization", it) }
			if (body != null) {
				connection.doOutput = true
				connection.setRequestProperty("Content-Type", "application/json")
				connection.outputStream.use { it.write(body.toByteArray()) }
			}
			val status = connection.responseCode
			val stream = if (status >= HTTP_ERROR) connection.errorStream else connection.inputStream
			HttpReply(status, stream?.use { it.readBytes().decodeToString() }.orEmpty())
		} catch (e: IOException) {
			HttpReply(-1, e.toString())
		} finally {
			connection.disconnect()
		}
	}

	/** Answers [status] to `POST` [route]; the stub with the lowest [priority] number that matches wins. */
	fun stubPost(route: String, status: Int, priority: Int, bodyContains: String? = null) {
		val bodyPattern = bodyContains?.let { ""","bodyPatterns":[{"contains":"$it"}]""" }.orEmpty()
		val mapping = """{"priority":$priority,"request":{"method":"POST","urlPath":"$route"$bodyPattern},""" +
			""""response":{"status":$status}}"""
		check(http("POST", "/__admin/mappings", mapping, null).status == HTTP_CREATED) { "the stub was not created" }
	}

	/** The app sending `POST` [route] with the Basic [credential] and [body]. */
	fun appPosts(route: String, credential: String, body: String) =
		http("POST", route, body, BasicAuth.header(credential))

	override fun close() {
		process.destroy()
		process.waitFor(STOP_MS, TimeUnit.MILLISECONDS)
	}

	private companion object {
		const val STARTUP_MS = 60_000L
		const val STOP_MS = 5_000L
		const val POLL_MS = 200L
		const val IO_MS = 5_000
		const val HTTP_OK = 200
		const val HTTP_CREATED = 201
		const val HTTP_ERROR = 400
	}
}
