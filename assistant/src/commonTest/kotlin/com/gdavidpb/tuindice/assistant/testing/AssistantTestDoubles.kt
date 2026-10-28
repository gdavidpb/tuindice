package com.gdavidpb.tuindice.assistant.testing

import com.gdavidpb.tuindice.assistant.data.repository.AssistantServerDisabledDataRepository
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.repository.AssistantConfigRepository
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineCapability
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.SSECapability
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.ResponseAdapterAttributeKey
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlin.time.Clock
import kotlin.time.Instant

// The app's client fails error answers by itself (expectSuccess), and resolves paths against the
// API root. A test that orders its requests with the coroutines passes [engineDispatcher], so the
// engine answers on the test's own dispatcher.
internal fun assistantHttpClient(
	engineDispatcher: CoroutineDispatcher? = null,
	handler: MockRequestHandler
): HttpClient {
	val config = MockEngineConfig().apply {
		addHandler(handler)
		dispatcher = engineDispatcher
	}

	return HttpClient(SseCapableMockEngine(config)) {
		expectSuccess = true

		install(SSE)

		defaultRequest {
			url("https://api.tuindice.test/")
		}
	}
}

// The SSE plugin asks its engine for the SSE capability, which the mock engine does not declare, and reads an SSE
// answer as the session the engine adapts from the body. This engine does both and keeps every other behavior.
@OptIn(InternalAPI::class)
private class SseCapableMockEngine(config: MockEngineConfig) : MockEngine(config) {
	override val supportedCapabilities: Set<HttpClientEngineCapability<out Any>> =
		super.supportedCapabilities + listOf<HttpClientEngineCapability<out Any>>(SSECapability)

	override suspend fun execute(data: HttpRequestData): HttpResponseData {
		val response = super.execute(data)
		val adapter = data.attributes.getOrNull(ResponseAdapterAttributeKey)
		val body = response.body as? ByteReadChannel
		if (adapter == null || body == null) return response
		val session = adapter.adapt(data, response.statusCode, response.headers, body, data.body, response.callContext)

		return HttpResponseData(
			statusCode = response.statusCode,
			requestTime = response.requestTime,
			headers = response.headers,
			version = response.version,
			body = session ?: body,
			callContext = response.callContext
		)
	}
}

internal fun MockRequestHandleScope.sseAnswer(body: String) = respond(
	content = body,
	status = HttpStatusCode.OK,
	headers = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())
)

internal fun MockRequestHandleScope.jsonAnswer(
	status: HttpStatusCode,
	body: String,
	retryAfter: String? = null
) = respond(
	content = body,
	status = status,
	headers = Headers.build {
		append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
		if (retryAfter != null) append(HttpHeaders.RetryAfter, retryAfter)
	}
)

internal const val HEARTBEAT = ": ping\n\n"

internal fun frame(seq: Long, json: String): String = "id: $seq\ndata: $json\n\n"

internal fun runStartedJson(seq: Long, turnId: String = "t1") =
	"""{"type":"run_started","seq":$seq,"turnId":"$turnId"}"""

internal fun progressJson(seq: Long, turnId: String = "t1") =
	"""{"type":"progress","seq":$seq,"turnId":"$turnId","tool":"subjects","label":"Buscando materias"}"""

internal fun runFinishedJson(seq: Long, turnId: String = "t1") =
	"""{"type":"run_finished","seq":$seq,"turnId":"$turnId"}"""

// The contract spells the code in lower case: "transient" decodes to TRANSIENT.
internal fun runErrorJson(seq: Long, turnId: String = "t1") =
	"""{"type":"run_error","seq":$seq,"turnId":"$turnId","code":"transient","retryable":true}"""

internal class RecordingServerDisabledRepository : AssistantServerDisabledDataRepository {
	val marks = mutableListOf<Int?>()

	override fun sessionMark(): Int = 0

	override fun markServerDisabled(retryAfterSeconds: Int?, sessionMark: Int) {
		marks += retryAfterSeconds
	}
}

// Reads the virtual time of the test, so the clock and the timers agree on when a window ends.
@OptIn(ExperimentalCoroutinesApi::class)
internal class VirtualClock(private val scheduler: TestCoroutineScheduler) : Clock {
	override fun now(): Instant = Instant.fromEpochMilliseconds(START_EPOCH_MILLIS + scheduler.currentTime)

	private companion object {
		const val START_EPOCH_MILLIS = 1_700_000_000_000L
	}
}

internal class FakeAssistantConfigRepository(
	mode: AssistantMode = AssistantMode.ON
) : AssistantConfigRepository {

	private val modes = MutableStateFlow(mode)

	// As the real source does, a subscriber receives the mode through a channel, after the call that subscribes: the first
	// value is not handed over by that call, and a value the configuration holds meanwhile reaches the subscriber later.
	override fun observeAssistantMode(): Flow<AssistantMode> = channelFlow {
		launch(start = CoroutineStart.UNDISPATCHED) {
			modes.collect { mode -> send(mode) }
		}
	}.distinctUntilChanged()

	fun setMode(mode: AssistantMode) {
		modes.value = mode
	}
}

internal class FakeSettings : Settings {
	private val values = mutableMapOf<String, Any>()

	override val keys: Set<String> get() = values.keys
	override val size: Int get() = values.size

	override fun clear() {
		values.clear()
	}

	override fun remove(key: String) {
		values.remove(key)
	}

	override fun hasKey(key: String): Boolean = key in values

	override fun putInt(key: String, value: Int) {
		values[key] = value
	}

	override fun getInt(key: String, defaultValue: Int): Int = values[key] as? Int ?: defaultValue

	override fun getIntOrNull(key: String): Int? = values[key] as? Int

	override fun putLong(key: String, value: Long) {
		values[key] = value
	}

	override fun getLong(key: String, defaultValue: Long): Long = values[key] as? Long ?: defaultValue

	override fun getLongOrNull(key: String): Long? = values[key] as? Long

	override fun putString(key: String, value: String) {
		values[key] = value
	}

	override fun getString(key: String, defaultValue: String): String = values[key] as? String ?: defaultValue

	override fun getStringOrNull(key: String): String? = values[key] as? String

	override fun putFloat(key: String, value: Float) {
		values[key] = value
	}

	override fun getFloat(key: String, defaultValue: Float): Float = values[key] as? Float ?: defaultValue

	override fun getFloatOrNull(key: String): Float? = values[key] as? Float

	override fun putDouble(key: String, value: Double) {
		values[key] = value
	}

	override fun getDouble(key: String, defaultValue: Double): Double = values[key] as? Double ?: defaultValue

	override fun getDoubleOrNull(key: String): Double? = values[key] as? Double

	override fun putBoolean(key: String, value: Boolean) {
		values[key] = value
	}

	override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
		values[key] as? Boolean ?: defaultValue

	override fun getBooleanOrNull(key: String): Boolean? = values[key] as? Boolean
}
