package com.gdavidpb.tuindice.enrollmentproof.testing

import com.gdavidpb.tuindice.base.domain.repository.FileRepository
import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.enrollmentproof.data.repository.DatabaseDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.repository.EnrollmentProofApiDataSource
import com.gdavidpb.tuindice.enrollmentproof.data.repository.StorageDataSource
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.presentation.resource.EnrollmentProofTextProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.Headers
import io.ktor.http.takeFrom
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.github.vinceglb.filekit.PlatformFile
import kotlin.coroutines.EmptyCoroutineContext

const val CURRENT_QUARTER_NAME = "2026-1"
const val DEFAULT_ENROLLMENT_PROOF_SOURCE = "/storage/enrollmentProofs/$CURRENT_QUARTER_NAME.pdf"
const val DEFAULT_ENROLLMENT_PROOF_CONTENT = "ZW5yb2xsbWVudC1wcm9vZg=="

val DEFAULT_ENROLLMENT_PROOF = EnrollmentProof(
	source = DEFAULT_ENROLLMENT_PROOF_SOURCE,
	content = DEFAULT_ENROLLMENT_PROOF_CONTENT
)

class FakeDatabaseDataSource(
	private val currentQuarterName: String? = CURRENT_QUARTER_NAME
) : DatabaseDataSource {
	override suspend fun getCurrentQuarterName(): String? = currentQuarterName
}

class FakeEnrollmentProofApiDataSource(
	private val enrollmentProof: EnrollmentProof = DEFAULT_ENROLLMENT_PROOF,
	private val throwable: Throwable? = null
) : EnrollmentProofApiDataSource {
	var invocationCount = 0
		private set
	var lastPassword: String? = null
		private set

	override suspend fun getEnrollmentProof(password: String): EnrollmentProof {
		invocationCount++
		lastPassword = password
		throwable?.let { throw it }
		return enrollmentProof
	}
}

class RecordingStorageDataSource(
	initialFiles: Map<String, EnrollmentProof> = emptyMap()
) : StorageDataSource {
	private val files = initialFiles.toMutableMap()

	val savedProofs = mutableListOf<Pair<String, EnrollmentProof>>()

	override suspend fun getEnrollmentProof(name: String): EnrollmentProof {
		return files[name] ?: throw EnrollmentProofNotFoundException()
	}

	override suspend fun enrollmentProofExists(name: String): Boolean = files.containsKey(name)

	override suspend fun saveEnrollmentProof(name: String, enrollmentProof: EnrollmentProof) {
		files[name] = enrollmentProof
		savedProofs += name to enrollmentProof
	}
}

class FakeNetworkRepository(
	private val isAvailable: Boolean
) : NetworkRepository {
	override fun isAvailable(): Boolean = isAvailable
}

class FakeEnrollmentProofRepository(
	private val enrollmentProof: EnrollmentProof = DEFAULT_ENROLLMENT_PROOF,
	private val throwable: Throwable? = null
) : EnrollmentProofRepository {
	var invocationCount = 0
		private set

	override suspend fun getEnrollmentProof(): EnrollmentProof {
		invocationCount++
		throwable?.let { throw it }
		return enrollmentProof
	}
}

class FakeFileRepository(
	private val canOpen: Boolean = true
) : FileRepository {
	var lastCanOpenFile: PlatformFile? = null

	override suspend fun canOpen(file: PlatformFile): Boolean {
		lastCanOpenFile = file
		return canOpen
	}
}

class RecordingReportingRepository : ReportingRepository {
	var identifier: String? = null
	val loggedExceptions = mutableListOf<Throwable>()
	val loggedMessages = mutableListOf<String>()
	val customKeys = mutableMapOf<String, Any>()

	override fun setIdentifier(identifier: String) {
		this.identifier = identifier
	}

	override fun logException(throwable: Throwable) {
		loggedExceptions += throwable
	}

	override fun logMessage(message: String) {
		loggedMessages += message
	}

	override fun <T : Any> setCustomKey(key: String, value: T) {
		customKeys[key] = value
	}
}

class FakeEnrollmentProofTextProvider : EnrollmentProofTextProvider {
	override fun serviceUnavailable(): String = "Servicio no disponible"

	override fun networkUnavailable(): String = "Comprueba tu conexión"

	override fun enrollmentNotFound(): String = "Comprobante no disponible"

	override fun enrollmentUnsupported(): String = "Archivo no soportado ;("

	override fun timeout(): String = "Tiempo de espera agotado"

	override fun defaultError(): String = "¡Ha ocurrido un error!"
}

@OptIn(InternalAPI::class)
fun clientRequestException(statusCode: HttpStatusCode): ClientRequestException {
	val client = HttpClient(MockEngine { respondOk() })
	val requestData = HttpRequestBuilder().apply {
		url.takeFrom("https://tuindice.test/enrollment-proof/v1")
	}.build()
	val responseData = HttpResponseData(
		statusCode = statusCode,
		requestTime = GMTDate(),
		headers = Headers.Empty,
		version = HttpProtocolVersion.HTTP_1_1,
		body = ByteReadChannel.Empty,
		callContext = EmptyCoroutineContext
	)
	val call = HttpClientCall(client, requestData, responseData)

	return ClientRequestException(call.response, statusCode.description)
}
