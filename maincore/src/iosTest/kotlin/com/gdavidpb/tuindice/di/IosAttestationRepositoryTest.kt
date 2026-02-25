package com.gdavidpb.tuindice.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.login.domain.model.IssueTokensAttestationPayload
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import platform.Foundation.NSTemporaryDirectory
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class IosAttestationRepositoryTest {
	@Test
	fun getAttestation_mapsProviderAndKeyId_andBuildsChallengeBoundInput() = runBlocking {
		val bridge = AttestationBridge(
			attestation = IosPlatformAttestation(
				token = "attestation-token",
				keyId = "key-id-1",
				provider = AttestationProvider.APP_ATTEST
			)
		)
		val client = createChallengeClient(
			challengeId = "challenge-id-1",
			challenge = "challenge-value-1"
		)

		withAttestationRepository(bridge = bridge, client = client) { repository ->
			val result = repository.getAttestation(
				payload = IssueTokensAttestationPayload(
					usbId = "20320000",
					password = "password-1"
				)
			)

			assertEquals("challenge-id-1", result.id)
			assertEquals("attestation-token", result.token)
			assertEquals("key-id-1", result.keyId)
			assertEquals(AttestationProvider.APP_ATTEST, result.provider)
			assertEquals(1, bridge.attestationInputs.size)

			val decodedInput = decodeBase64Url(bridge.attestationInputs.single())
			val inputJson = Json.parseToJsonElement(decodedInput).jsonObject
			assertEquals("challenge-value-1", inputJson["challenge"]?.jsonPrimitive?.content)

			val payloadJson = assertNotNull(inputJson["payload"]).jsonObject
			assertEquals(
				"issue_tokens_attestation",
				payloadJson["type"]?.jsonPrimitive?.content
			)
			assertEquals("20320000", payloadJson["usb_id"]?.jsonPrimitive?.content)
			assertEquals("password-1", payloadJson["password"]?.jsonPrimitive?.content)
		}
	}

	@Test
	fun getAttestation_whenBridgeReturnsNull_throwsIllegalStateException() = runBlocking {
		val bridge = AttestationBridge(attestation = null)
		val client = createChallengeClient(
			challengeId = "challenge-id-2",
			challenge = "challenge-value-2"
		)

		withAttestationRepository(bridge = bridge, client = client) { repository ->
			assertFailsWith<IllegalStateException> {
				repository.getAttestation(
					payload = IssueTokensAttestationPayload(
						usbId = "20320001",
						password = "password-2"
					)
				)
			}

			assertEquals(1, bridge.attestationInputs.size)
		}
	}

	@Test
	fun getAttestation_whenProviderIsNotAppAttest_throwsIllegalStateException() = runBlocking {
		val bridge = AttestationBridge(
			attestation = IosPlatformAttestation(
				token = "attestation-token",
				keyId = "key-id-2",
				provider = AttestationProvider.PLAY_INTEGRITY
			)
		)
		val client = createChallengeClient(
			challengeId = "challenge-id-3",
			challenge = "challenge-value-3"
		)

		withAttestationRepository(bridge = bridge, client = client) { repository ->
			assertFailsWith<IllegalStateException> {
				repository.getAttestation(
					payload = IssueTokensAttestationPayload(
						usbId = "20320002",
						password = "password-3"
					)
				)
			}

			assertEquals(1, bridge.attestationInputs.size)
		}
	}

	@Test
	fun getAttestation_whenKeyIdIsNull_throwsIllegalStateException() = runBlocking {
		val bridge = AttestationBridge(
			attestation = IosPlatformAttestation(
				token = "attestation-token",
				keyId = null,
				provider = AttestationProvider.APP_ATTEST
			)
		)
		val client = createChallengeClient(
			challengeId = "challenge-id-4",
			challenge = "challenge-value-4"
		)

		withAttestationRepository(bridge = bridge, client = client) { repository ->
			assertFailsWith<IllegalStateException> {
				repository.getAttestation(
					payload = IssueTokensAttestationPayload(
						usbId = "20320003",
						password = "password-4"
					)
				)
			}

			assertEquals(1, bridge.attestationInputs.size)
		}
	}

	@Test
	fun getAttestation_whenKeyIdIsBlank_throwsIllegalStateException() = runBlocking {
		val bridge = AttestationBridge(
			attestation = IosPlatformAttestation(
				token = "attestation-token",
				keyId = "  ",
				provider = AttestationProvider.APP_ATTEST
			)
		)
		val client = createChallengeClient(
			challengeId = "challenge-id-5",
			challenge = "challenge-value-5"
		)

		withAttestationRepository(bridge = bridge, client = client) { repository ->
			assertFailsWith<IllegalStateException> {
				repository.getAttestation(
					payload = IssueTokensAttestationPayload(
						usbId = "20320004",
						password = "password-5"
					)
				)
			}

			assertEquals(1, bridge.attestationInputs.size)
		}
	}

	private fun withAttestationRepository(
		bridge: AttestationBridge,
		client: HttpClient,
		block: suspend (AttestationRepository) -> Unit
	) = runBlocking {
		val dataStorePath = testDataStorePath()
		val dataStore = createTestDataStore(path = dataStorePath)
		val koinApp = koinApplication {
			allowOverride(true)
			modules(
				iosPlatformModule(
					config = IosPlatformConfig(
						bridge = bridge,
						dataStore = dataStore
					)
				),
				module {
					single<HttpClient> { client }
				}
			)
		}

		try {
			block(koinApp.koin.get())
		} finally {
			koinApp.close()
			FileSystem.SYSTEM.delete(dataStorePath, mustExist = false)
		}
	}

	private fun createChallengeClient(
		challengeId: String,
		challenge: String
	): HttpClient {
		return HttpClient(MockEngine {
			respond(
				content = """
					{
					  "id": "$challengeId",
					  "challenge": "$challenge"
					}
				""".trimIndent(),
				status = HttpStatusCode.OK,
				headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
			)
		}) {
			install(ContentNegotiation) {
				json(createSharedJson())
			}
		}
	}

	private fun createTestDataStore(path: Path): DataStore<Preferences> {
		return PreferenceDataStoreFactory.createWithPath(
			scope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
			produceFile = {
				FileSystem.SYSTEM.createDirectories(path.parent!!)
				path
			}
		)
	}

	private fun testDataStorePath(): Path {
		return "${NSTemporaryDirectory().trimEnd('/')}/tuindice-tests/attestation-${Random.nextLong().toString().replace("-", "n")}.preferences_pb"
			.toPath()
	}

	@OptIn(ExperimentalEncodingApi::class)
	private fun decodeBase64Url(value: String): String {
		return Base64.UrlSafe.decode(value).decodeToString()
	}
}

private class AttestationBridge(
	private val attestation: IosPlatformAttestation?
) : IosPlatformBridge {
	val attestationInputs = mutableListOf<String>()

	override suspend fun fetchRemoteConfig() = Unit

	override fun remoteConfigString(key: String): String? = null

	override fun remoteConfigStringList(key: String): List<String>? = null

	override suspend fun requestAttestation(attestationInput: String): IosPlatformAttestation? {
		attestationInputs += attestationInput
		return attestation
	}

	override suspend fun pushToken(): String? = null

	override suspend fun launchReview() = Unit

	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? = null

	override suspend fun launchUpdate(action: UpdateAction) = Unit

	override fun openUrl(url: String) = Unit

	override fun openStorePage() = Unit

	override fun openFile(fileRef: PlatformFileRef): Boolean = false

	override fun canOpen(fileRef: PlatformFileRef): Boolean = false

	override fun sendEmail(email: String, subject: String, text: String) = Unit

	override fun shareText(subject: String, text: String) = Unit

	override fun appVersionName(): String = "1.0.0"

	override fun appVersionCode(): Long = 1L

	override fun hasCamera(): Boolean = false

	override fun isNetworkAvailable(): Boolean = true

	override fun setUserIdentifier(identifier: String) = Unit

	override fun logMessage(message: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun setCustomKey(key: String, value: String) = Unit

	override fun restartDependencies() = Unit

	override fun secureStoreContains(key: String): Boolean = false

	override fun secureStoreGetString(key: String): String? = null

	override fun secureStorePutString(key: String, value: String) = Unit

	override fun secureStoreClear() = Unit
}
