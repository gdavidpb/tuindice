package com.gdavidpb.tuindice.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.login.domain.repository.MessagingRepository as LoginMessagingRepository
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
import kotlin.test.assertFailsWith

class IosLoginMessagingRepositoryTest {
	@Test
	fun getToken_withNonBlankValue_returnsBridgeToken() = runBlocking {
		val bridge = LoginMessagingBridge(token = "ios-token-1")

		withRepository(bridge) { repository ->
			assertEquals("ios-token-1", repository.getToken())
		}
	}

	@Test
	fun getToken_withNullValue_throwsIllegalStateException() = runBlocking {
		val bridge = LoginMessagingBridge(token = null)

		withRepository(bridge) { repository ->
			assertFailsWith<IllegalStateException> {
				repository.getToken()
			}
		}
	}

	@Test
	fun getToken_withBlankValue_throwsIllegalStateException() = runBlocking {
		val bridge = LoginMessagingBridge(token = "   ")

		withRepository(bridge) { repository ->
			assertFailsWith<IllegalStateException> {
				repository.getToken()
			}
		}
	}

	private fun withRepository(
		bridge: LoginMessagingBridge,
		block: suspend (LoginMessagingRepository) -> Unit
	) = runBlocking {
		val dataStorePath = testDataStorePath()
		val dataStore = createTestDataStore(dataStorePath)
		val app = koinApplication {
			modules(
				iosPlatformModule(
					config = IosPlatformConfig(
						bridge = bridge,
						dataStore = dataStore
					)
				)
			)
		}

		try {
			block(app.koin.get())
		} finally {
			app.close()
			FileSystem.SYSTEM.delete(dataStorePath, mustExist = false)
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
		return "${NSTemporaryDirectory().trimEnd('/')}/tuindice-tests/login-push-${Random.nextLong().toString().replace("-", "n")}.preferences_pb"
			.toPath()
	}
}

private class LoginMessagingBridge(
	private val token: String?
) : IosPlatformBridge {
	override suspend fun fetchRemoteConfig() = Unit

	override fun remoteConfigString(key: String): String? = null

	override fun remoteConfigStringList(key: String): List<String>? = null

	override suspend fun requestAttestation(attestationInput: String): IosPlatformAttestation? = null

	override suspend fun pushToken(): String? = token

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
