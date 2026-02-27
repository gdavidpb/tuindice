package com.gdavidpb.tuindice.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.login.domain.repository.ReportingRepository as LoginReportingRepository
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
import kotlin.test.assertIs

class IosReportingRepositoryTest {
	@Test
	fun reportingGateways_delegateToBridge() = runBlocking {
		val bridge = ReportingBridge()

		withReportingDependencies(bridge = bridge) { reportingGateway, loginReporting ->
			reportingGateway.setIdentifier("uid-1")
			loginReporting.setIdentifier("uid-2")
			reportingGateway.logMessage("message-1")
			reportingGateway.setCustomKey("flow", "ios-smoke")
			reportingGateway.logException(IllegalStateException("boom"))

			assertEquals(listOf("uid-1", "uid-2"), bridge.identifiers)
			assertEquals(listOf("message-1"), bridge.messages)
			assertEquals(mapOf("flow" to "ios-smoke"), bridge.customKeys)
			assertEquals(1, bridge.exceptions.size)
			assertIs<IllegalStateException>(bridge.exceptions.first())
		}
	}

	private fun withReportingDependencies(
		bridge: ReportingBridge,
		block: suspend (
			reportingGateway: ReportingRepository,
			loginReporting: LoginReportingRepository
		) -> Unit
	) = runBlocking {
		val dataStorePath = testDataStorePath()
		val dataStore = createTestDataStore(path = dataStorePath)
		val koinApp = koinApplication {
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
			block(
				koinApp.koin.get(),
				koinApp.koin.get()
			)
		} finally {
			koinApp.close()
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
		return "${NSTemporaryDirectory().trimEnd('/')}/tuindice-tests/reporting-${Random.nextLong().toString().replace("-", "n")}.preferences_pb"
			.toPath()
	}
}

private class ReportingBridge : IosPlatformBridge {
	val identifiers = mutableListOf<String>()
	val messages = mutableListOf<String>()
	val exceptions = mutableListOf<Throwable>()
	val customKeys = mutableMapOf<String, String>()

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

	override fun setUserIdentifier(identifier: String) {
		identifiers += identifier
	}

	override fun logMessage(message: String) {
		messages += message
	}

	override fun logException(throwable: Throwable) {
		exceptions += throwable
	}

	override fun setCustomKey(key: String, value: String) {
		customKeys[key] = value
	}

	override fun restartDependencies() = Unit

	override fun secureStoreContains(key: String): Boolean = false

	override fun secureStoreGetString(key: String): String? = null

	override fun secureStorePutString(key: String, value: String) = Unit

	override fun secureStoreClear() = Unit
}
