package com.gdavidpb.tuindice.data.source.application

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.domain.model.IosPlatformAttestation
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceMaintenanceRepository
import com.gdavidpb.tuindice.platform.IosAttestationCapability
import com.gdavidpb.tuindice.platform.IosExternalActionsCapability
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class IosApplicationDataSourceTest {
	@Test
	fun clearData_whenDatabaseWipeFails_keepsTheLocalDataOwnerMarker() = runTest {
		val settingsRepository = FakeSettingsRepository(localDataOwner = "20-26123")
		val dataSource = dataSource(
			persistenceMaintenanceRepository = FailingPersistenceMaintenanceRepository(),
			settingsRepository = settingsRepository
		)

		assertFailsWith<IllegalStateException> { dataSource.clearData() }

		assertEquals("20-26123", settingsRepository.getLocalDataOwner())
		assertEquals(false, settingsRepository.cleared)
	}

	@Test
	fun clearData_whenEverythingSucceeds_dropsTheLocalDataOwnerMarker() = runTest {
		val settingsRepository = FakeSettingsRepository(localDataOwner = "20-26123")
		val dataSource = dataSource(
			persistenceMaintenanceRepository = NoOpPersistenceMaintenanceRepository(),
			settingsRepository = settingsRepository
		)

		dataSource.clearData()

		assertNull(settingsRepository.getLocalDataOwner())
		assertEquals(true, settingsRepository.cleared)
	}

	private fun dataSource(
		persistenceMaintenanceRepository: PersistenceMaintenanceRepository,
		settingsRepository: FakeSettingsRepository
	) = IosApplicationDataSource(
		persistenceMaintenanceRepository = persistenceMaintenanceRepository,
		settingsRepository = settingsRepository,
		secureStore = NoOpSecureKeyValueDataRepository(),
		legacySecureStore = NoOpSecureKeyValueDataRepository(),
		attestationCapability = NoOpAttestationCapability(),
		externalActionsCapability = NoOpExternalActionsCapability()
	)
}

private class FailingPersistenceMaintenanceRepository : PersistenceMaintenanceRepository {
	override suspend fun clearAll(): Unit = error("database wipe failed")
}

private class NoOpPersistenceMaintenanceRepository : PersistenceMaintenanceRepository {
	override suspend fun clearAll() = Unit
}

private class NoOpSecureKeyValueDataRepository : SecureKeyValueDataRepository {
	override suspend fun getString(key: String): String? = null

	override suspend fun putString(key: String, value: String) = Unit

	override suspend fun remove(key: String) = Unit

	override suspend fun clear() = Unit
}

private class NoOpAttestationCapability : IosAttestationCapability {
	override fun sha256Base64Url(value: String): String? = null

	override suspend fun resolveAttestationKeyId(): String? = null

	override suspend fun invalidateAttestationKeyId() = Unit

	override suspend fun requestAttestation(
		attestationInput: String,
		keyId: String,
		evidenceMode: String
	): IosPlatformAttestation? = null
}

private class NoOpExternalActionsCapability : IosExternalActionsCapability {
	override fun openUrl(url: String) = Unit

	override fun openFile(path: String): Boolean = false

	override fun canOpen(path: String): Boolean = false
}
