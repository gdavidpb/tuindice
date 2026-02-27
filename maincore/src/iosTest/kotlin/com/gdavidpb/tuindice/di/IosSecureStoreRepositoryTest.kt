package com.gdavidpb.tuindice.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.SecureStoreRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
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
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import platform.Foundation.NSTemporaryDirectory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IosSecureStoreRepositoryTest {
	@Test
	fun defaultBridge_secureStoreApis_throwMissingBridgeError() {
		assertFailsWith<IllegalStateException> {
			DefaultIosPlatformBridge.secureStoreContains("key")
		}
		assertFailsWith<IllegalStateException> {
			DefaultIosPlatformBridge.secureStoreGetString("key")
		}
		assertFailsWith<IllegalStateException> {
			DefaultIosPlatformBridge.secureStorePutString("key", "value")
		}
		assertFailsWith<IllegalStateException> {
			DefaultIosPlatformBridge.secureStoreClear()
		}
	}

	@Test
	fun secureStore_missingKey_doesNotTriggerImplicitMigrationWrite() = runBlocking {
		withIosApp { koin, bridge ->
			val secureStore = koin.get<SecureStoreRepository>()

			assertFalse(secureStore.contains("missing"))
			assertNull(secureStore.getString("missing"))
			assertEquals(0, bridge.putCalls)
		}
	}

	@Test
	fun sessionClear_removesPersistedCredentialsFromSecureStoreBridge() = runBlocking {
		withIosApp { koin, bridge ->
			val sessionRepository = koin.get<SessionRepository>()

			sessionRepository.setUsbId("20320000")
			sessionRepository.setAccessToken("access-token")
			sessionRepository.setRefreshToken("refresh-token")

			assertTrue(sessionRepository.hasActiveSession())
			assertTrue(bridge.containsKey(PreferencesKeys.USER_USB_ID))
			assertTrue(bridge.containsKey(PreferencesKeys.USER_ACCESS_TOKEN))
			assertTrue(bridge.containsKey(PreferencesKeys.USER_REFRESH_TOKEN))

			sessionRepository.clear()

			assertFalse(sessionRepository.hasActiveSession())
			assertFalse(bridge.containsKey(PreferencesKeys.USER_USB_ID))
			assertFalse(bridge.containsKey(PreferencesKeys.USER_ACCESS_TOKEN))
			assertFalse(bridge.containsKey(PreferencesKeys.USER_REFRESH_TOKEN))
			assertEquals(1, bridge.clearCalls)
		}
	}

	private fun withIosApp(
		block: suspend (koin: Koin, bridge: SecureStoreBridge) -> Unit
	) = runBlocking {
		val bridge = SecureStoreBridge()
		val dataStorePath = testDataStorePath()
		val dataStore = createTestDataStore(dataStorePath)

		val app = koinApplication {
			allowOverride(true)
			modules(
				iosPlatformModule(
					config = IosPlatformConfig(
						bridge = bridge,
						dataStore = dataStore
					)
				),
				module {
					single<HttpClient> { createMockClient() }
				}
			)
		}

		try {
			block(app.koin, bridge)
		} finally {
			app.close()
			FileSystem.SYSTEM.delete(dataStorePath, mustExist = false)
		}
	}

	private fun createMockClient(): HttpClient {
		return HttpClient(MockEngine {
			respond(
				content = "{}",
				status = HttpStatusCode.OK,
				headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
			)
		}) {
			install(ContentNegotiation) {
				json(Json { ignoreUnknownKeys = true })
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
		return "${NSTemporaryDirectory().trimEnd('/')}/tuindice-tests/secure-store-${Random.nextLong().toString().replace("-", "n")}.preferences_pb"
			.toPath()
	}
}

private class SecureStoreBridge : IosPlatformBridge {
	private val values = mutableMapOf<String, String>()

	var putCalls: Int = 0
		private set

	var clearCalls: Int = 0
		private set

	fun containsKey(key: String): Boolean = values.containsKey(key)

	override suspend fun fetchRemoteConfig() = Unit

	override fun remoteConfigString(key: String): String? = null

	override fun remoteConfigStringList(key: String): List<String>? = null

	override suspend fun requestAttestation(attestationInput: String): IosPlatformAttestation? = null

	override suspend fun pushToken(): String? = null

	override suspend fun launchReview() = Unit

	override suspend fun checkForUpdate(stalenessDays: Int): UpdateAction? = null

	override suspend fun launchUpdate(action: UpdateAction) = Unit

	override fun openUrl(url: String) = Unit

	override fun openStorePage() = Unit

	override fun openFile(fileRef: PlatformFileRef): Boolean = false

	override fun canOpen(fileRef: PlatformFileRef): Boolean = false

	override fun appVersionName(): String = "1.0.0"

	override fun appVersionCode(): Long = 1L

	override fun hasCamera(): Boolean = false

	override fun isNetworkAvailable(): Boolean = true

	override fun setUserIdentifier(identifier: String) = Unit

	override fun logMessage(message: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun setCustomKey(key: String, value: String) = Unit

	override fun restartDependencies() = Unit

	override fun secureStoreContains(key: String): Boolean = values.containsKey(key)

	override fun secureStoreGetString(key: String): String? = values[key]

	override fun secureStorePutString(key: String, value: String) {
		putCalls += 1
		values[key] = value
	}

	override fun secureStoreClear() {
		clearCalls += 1
		values.clear()
	}
}
