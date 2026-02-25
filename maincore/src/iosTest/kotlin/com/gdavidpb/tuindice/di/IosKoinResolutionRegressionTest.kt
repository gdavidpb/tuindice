package com.gdavidpb.tuindice.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.gdavidpb.tuindice.base.domain.model.AttestationProvider
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.base.domain.repository.MessagingRepository
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignOutViewModel
import com.gdavidpb.tuindice.persistence.di.createIosDatabase
import com.gdavidpb.tuindice.persistence.di.persistenceCommonModule
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
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
import kotlin.test.assertNotNull

class IosKoinResolutionRegressionTest {
	@Test
	fun resolvesMessagingAndAttestationGraphWithoutRecursiveOverflow() {
		runBlocking {
			val dataStorePath = temporaryPath("resolution-datastore", "preferences_pb")
			val databasePath = temporaryPath("resolution-database", "db")
			val dataStore = createTestDataStore(path = dataStorePath)
			val bridge = ResolutionBridge()

			val koinApp = koinApplication {
				modules(
					iosSharedModules(
						platformConfig = IosPlatformConfig(
							bridge = bridge,
							dataStore = dataStore
						),
						extraModules = listOf(
							persistenceCommonModule(
								database = createIosDatabase(path = databasePath.toString())
							)
						)
					)
				)
			}

			try {
				val koin = koinApp.koin

				assertNotNull(koin.get<MessagingRepository>())
				assertNotNull(koin.get<AttestationRepository>())
				assertNotNull(koin.get<SignOutViewModel>())
				assertNotNull(koin.get<MainViewModel>())
			} finally {
				koinApp.close()
				FileSystem.SYSTEM.delete(databasePath, mustExist = false)
				FileSystem.SYSTEM.delete(dataStorePath, mustExist = false)
			}
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

	private fun temporaryPath(
		prefix: String,
		suffix: String
	): Path {
		val token = Random.nextLong().toString().replace("-", "n")
		return "${NSTemporaryDirectory().trimEnd('/')}/tuindice-tests/$prefix-$token.$suffix".toPath()
	}
}

private class ResolutionBridge : IosPlatformBridge {
	private val secureStore = mutableMapOf<String, String>()

	override fun remoteConfigString(key: String): String? = null

	override fun remoteConfigStringList(key: String): List<String>? = null

	override suspend fun requestAttestation(attestationInput: String): IosPlatformAttestation {
		return IosPlatformAttestation(
			token = "ios-attestation-token",
			keyId = "ios-attestation-key-id",
			provider = AttestationProvider.APP_ATTEST
		)
	}

	override suspend fun pushToken(): String = "ios-push-token"

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

	override fun secureStoreContains(key: String): Boolean = secureStore.containsKey(key)

	override fun secureStoreGetString(key: String): String? = secureStore[key]

	override fun secureStorePutString(key: String, value: String) {
		secureStore[key] = value
	}

	override fun secureStoreClear() {
		secureStore.clear()
	}
}
