package com.gdavidpb.tuindice.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import platform.Foundation.NSTemporaryDirectory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class IosMessagingRepositoryTest {
	@Test
	fun subscribe_postsTokenOnlyOnce_andPersistsSubscribedFlag() = runBlocking {
		val bridge = PushBridge(token = "ios-token-1")
		var postCalls = 0
		var deleteCalls = 0

		val mockClient = createMockClient(
			onMethod = { method ->
				if (method == HttpMethod.Post) postCalls++
				if (method == HttpMethod.Delete) deleteCalls++
			}
		)

		withMessagingRepository(bridge = bridge, client = mockClient) { repository, dataStore ->
			repository.subscribe()
			repository.subscribe()

			assertEquals(1, postCalls)
			assertEquals(0, deleteCalls)
			assertEquals(
				true,
				dataStore.data.first()[booleanPreferencesKey(PreferencesKeys.IS_SUBSCRIBED)]
			)
			assertEquals(
				"ios-token-1",
				dataStore.data.first()[stringPreferencesKey(PreferencesKeys.PUSH_TOKEN)]
			)
		}
	}

	@Test
	fun subscribe_withMissingToken_doesNotPersistSubscription() = runBlocking {
		val bridge = PushBridge(token = null)
		var postCalls = 0

		val mockClient = createMockClient(
			onMethod = { method ->
				if (method == HttpMethod.Post) postCalls++
			}
		)

		withMessagingRepository(bridge = bridge, client = mockClient) { repository, dataStore ->
			repository.subscribe()

			assertEquals(0, postCalls)
			assertEquals(
				null,
				dataStore.data.first()[booleanPreferencesKey(PreferencesKeys.IS_SUBSCRIBED)]
			)
		}
	}

	@Test
	fun subscribe_withBlankToken_doesNotPersistSubscription() = runBlocking {
		val bridge = PushBridge(token = "   ")
		var postCalls = 0

		val mockClient = createMockClient(
			onMethod = { method ->
				if (method == HttpMethod.Post) postCalls++
			}
		)

		withMessagingRepository(bridge = bridge, client = mockClient) { repository, dataStore ->
			repository.subscribe()

			assertEquals(0, postCalls)
			assertEquals(
				null,
				dataStore.data.first()[booleanPreferencesKey(PreferencesKeys.IS_SUBSCRIBED)]
			)
			assertEquals(
				null,
				dataStore.data.first()[stringPreferencesKey(PreferencesKeys.PUSH_TOKEN)]
			)
		}
	}

	@Test
	fun unsubscribe_invokesDeleteWithoutThrowing() = runBlocking {
		val bridge = PushBridge(token = "ios-token-2")
		var deleteCalls = 0

		val mockClient = createMockClient(
			onMethod = { method ->
				if (method == HttpMethod.Delete) deleteCalls++
			}
		)

		withMessagingRepository(bridge = bridge, client = mockClient) { repository, _ ->
			repository.subscribe()
			repository.unsubscribe()

			assertEquals(1, deleteCalls)
		}
	}

	@Test
	fun subscribe_whenTokenRotates_postsAgainAndUpdatesStoredToken() = runBlocking {
		val bridge = PushBridge(token = "ios-token-1")
		var postCalls = 0
		val mockClient = createMockClient(
			onMethod = { method ->
				if (method == HttpMethod.Post) postCalls++
			}
		)

		withMessagingRepository(bridge = bridge, client = mockClient) { repository, dataStore ->
			repository.subscribe()
			bridge.updateToken("ios-token-2")
			repository.subscribe()

			assertEquals(2, postCalls)
			assertEquals(
				"ios-token-2",
				dataStore.data.first()[stringPreferencesKey(PreferencesKeys.PUSH_TOKEN)]
			)
		}
	}

	@Test
	fun unsubscribe_clearsSubscriptionState() = runBlocking {
		val bridge = PushBridge(token = "ios-token-3")
		val mockClient = createMockClient(onMethod = {})

		withMessagingRepository(bridge = bridge, client = mockClient) { repository, dataStore ->
			repository.subscribe()
			repository.unsubscribe()

			assertEquals(
				null,
				dataStore.data.first()[booleanPreferencesKey(PreferencesKeys.IS_SUBSCRIBED)]
			)
			assertEquals(
				null,
				dataStore.data.first()[stringPreferencesKey(PreferencesKeys.PUSH_TOKEN)]
			)
		}
	}

	@Test
	fun unsubscribe_whenDeleteFails_stillClearsSubscriptionState() = runBlocking {
		val bridge = PushBridge(token = "ios-token-4")
		var deleteCalls = 0
		val mockClient = createMockClient(
			onMethod = { method ->
				if (method == HttpMethod.Delete) deleteCalls++
			},
			failOnDelete = true
		)

		withMessagingRepository(bridge = bridge, client = mockClient) { repository, dataStore ->
			repository.subscribe()
			repository.unsubscribe()

			assertEquals(1, deleteCalls)
			assertEquals(
				null,
				dataStore.data.first()[booleanPreferencesKey(PreferencesKeys.IS_SUBSCRIBED)]
			)
			assertEquals(
				null,
				dataStore.data.first()[stringPreferencesKey(PreferencesKeys.PUSH_TOKEN)]
			)
		}
	}

	private fun withMessagingRepository(
		bridge: PushBridge,
		client: HttpClient,
		block: suspend (
			repository: MessagingRepository,
			dataStore: DataStore<Preferences>
		) -> Unit
	) = runBlocking {
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
					single<HttpClient> { client }
				}
			)
		}

		try {
			block(app.koin.get(), dataStore)
		} finally {
			app.close()
			FileSystem.SYSTEM.delete(dataStorePath, mustExist = false)
		}
	}

	private fun createMockClient(
		onMethod: (HttpMethod) -> Unit,
		failOnDelete: Boolean = false
	): HttpClient {
		return HttpClient(MockEngine { request ->
			onMethod(request.method)
			if (failOnDelete && request.method == HttpMethod.Delete) {
				throw IllegalStateException("delete-failed")
			}

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
		return "${NSTemporaryDirectory().trimEnd('/')}/tuindice-tests/push-${Random.nextLong().toString().replace("-", "n")}.preferences_pb"
			.toPath()
	}
}

private class PushBridge(
	private val token: String?
) : IosPlatformBridge {
	private var mutableToken: String? = token

	fun updateToken(token: String?) {
		mutableToken = token
	}

	override suspend fun fetchRemoteConfig() = Unit

	override fun remoteConfigString(key: String): String? = null

	override fun remoteConfigStringList(key: String): List<String>? = null

	override suspend fun requestAttestation(attestationInput: String): IosPlatformAttestation? = null

	override suspend fun pushToken(): String? = mutableToken

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
