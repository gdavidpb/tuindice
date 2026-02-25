package com.gdavidpb.tuindice.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.koin.dsl.koinApplication
import platform.Foundation.NSTemporaryDirectory
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class IosConfigRepositoryTest {
	@Test
	fun usesDefaultValuesWhenRemoteConfigIsMissing() = runBlocking {
		val bridge = TestBridge()
		val defaults = IosConfigValues(
			timeoutMillis = 12_345L,
			contactEmail = "team@tuindice.app",
			contactSubject = "Support Subject",
			loadingMessages = listOf("A", "B"),
			updateStalenessDays = 9,
			syncsToSuggestReview = 4
		)

		withConfigRepository(bridge = bridge, defaults = defaults) { repository ->
			repository.tryFetch()

			assertEquals(1, bridge.fetchCalls)
			assertEquals(defaults.timeoutMillis, repository.getTimeout())
			assertEquals(defaults.contactEmail, repository.getContactEmail())
			assertEquals(defaults.contactSubject, repository.getContactSubject())
			assertEquals(defaults.loadingMessages, repository.getLoadingMessages())
			assertEquals(defaults.updateStalenessDays, repository.getTimeUpdateStalenessDays())
			assertEquals(defaults.syncsToSuggestReview, repository.getSyncsToSuggestReview())
		}
	}

	@Test
	fun appliesRemoteConfigOverridesAndFallsBackOnInvalidNumerics() = runBlocking {
		val bridge = TestBridge().apply {
			remoteStrings = mapOf(
				"time_out_connection" to "not-a-number",
				"contact_email" to "remote@tuindice.app",
				"contact_subject" to "Remote Subject",
				"time_update_staleness_days" to "15",
				"syncs_to_suggest_review" to "invalid"
			)
			remoteLists = mapOf(
				"loading_messages" to listOf("Remote 1", "Remote 2")
			)
		}
		val defaults = IosConfigValues(
			timeoutMillis = 90_000L,
			contactEmail = "default@tuindice.app",
			contactSubject = "Default Subject",
			loadingMessages = listOf("Default"),
			updateStalenessDays = 7,
			syncsToSuggestReview = 3
		)

		withConfigRepository(bridge = bridge, defaults = defaults) { repository ->
			repository.tryFetch()

			assertEquals(1, bridge.fetchCalls)
			assertEquals(defaults.timeoutMillis, repository.getTimeout())
			assertEquals("remote@tuindice.app", repository.getContactEmail())
			assertEquals("Remote Subject", repository.getContactSubject())
			assertEquals(listOf("Remote 1", "Remote 2"), repository.getLoadingMessages())
			assertEquals(15, repository.getTimeUpdateStalenessDays())
			assertEquals(defaults.syncsToSuggestReview, repository.getSyncsToSuggestReview())
		}
	}

	private fun withConfigRepository(
		bridge: TestBridge,
		defaults: IosConfigValues,
		block: suspend (ConfigRepository) -> Unit
	) = runBlocking {
		val dataStorePath = testDataStorePath()
		val dataStore = createTestDataStore(path = dataStorePath)
		val koinApp = koinApplication {
			modules(
				iosPlatformModule(
					config = IosPlatformConfig(
						configValues = defaults,
						bridge = bridge,
						dataStore = dataStore
					)
				)
			)
		}

		try {
			block(koinApp.koin.get())
		} finally {
			koinApp.close()
			FileSystem.SYSTEM.delete(dataStorePath, mustExist = false)
		}
	}

	private fun createTestDataStore(
		path: Path
	): DataStore<Preferences> {
		return PreferenceDataStoreFactory.createWithPath(
			scope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
			produceFile = {
				FileSystem.SYSTEM.createDirectories(path.parent!!)
				path
			}
		)
	}

	private fun testDataStorePath(): Path {
		return "${NSTemporaryDirectory().trimEnd('/')}/tuindice-tests/config-${Random.nextLong().toString().replace("-", "n")}.preferences_pb"
			.toPath()
	}
}

private class TestBridge : IosPlatformBridge {
	var fetchCalls: Int = 0
	var remoteStrings: Map<String, String> = emptyMap()
	var remoteLists: Map<String, List<String>> = emptyMap()

	override suspend fun fetchRemoteConfig() {
		fetchCalls++
	}

	override fun remoteConfigString(key: String): String? = remoteStrings[key]

	override fun remoteConfigStringList(key: String): List<String>? = remoteLists[key]

	override suspend fun requestAttestation(attestationInput: String): IosPlatformAttestation? = null

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
