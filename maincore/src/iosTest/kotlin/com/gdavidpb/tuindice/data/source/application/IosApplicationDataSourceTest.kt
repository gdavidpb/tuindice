package com.gdavidpb.tuindice.data.source.application

import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.domain.session.SessionResidue
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

	@Test
	fun clearData_whenEverythingSucceeds_clearsEverySessionMemoryAfterTheStores() = runTest {
		val settingsRepository = FakeSettingsRepository(localDataOwner = "20-26123")
		val holders = listOf(
			RecordingSessionMemory(settingsRepository),
			RecordingSessionMemory(settingsRepository)
		)
		val dataSource = dataSource(
			persistenceMaintenanceRepository = NoOpPersistenceMaintenanceRepository(),
			settingsRepository = settingsRepository,
			sessionMemory = holders
		)

		dataSource.clearData()

		assertEquals(listOf(true, true), holders.map(RecordingSessionMemory::clearedAfterTheStores))
	}

	@Test
	fun clearData_whenDatabaseWipeFails_stillClearsEverySessionMemory() = runTest {
		val settingsRepository = FakeSettingsRepository(localDataOwner = "20-26123")
		val holders = listOf(
			RecordingSessionMemory(settingsRepository, failure = IllegalStateException("holder failed")),
			RecordingSessionMemory(settingsRepository)
		)
		val dataSource = dataSource(
			persistenceMaintenanceRepository = FailingPersistenceMaintenanceRepository(),
			settingsRepository = settingsRepository,
			sessionMemory = holders
		)

		val failure = assertFailsWith<IllegalStateException> { dataSource.clearData() }

		assertEquals("database wipe failed", failure.message)
		assertEquals(listOf(1, 1), holders.map(RecordingSessionMemory::clearCount))
	}

	// Only the residue contract is asked: the stores and the session memory stay as they are.
	@Test
	fun clearSessionResidue_asksEveryHolderAndThrowsTheFirstFailureAfterwards() = runTest {
		val settingsRepository = FakeSettingsRepository()
		val residue = listOf(
			RecordingSessionResidue(failure = IllegalStateException("first failed")),
			RecordingSessionResidue(failure = IllegalStateException("second failed")),
			RecordingSessionResidue()
		)
		val memory = RecordingSessionMemory(settingsRepository)
		val dataSource = dataSource(
			persistenceMaintenanceRepository = NoOpPersistenceMaintenanceRepository(),
			settingsRepository = settingsRepository,
			sessionMemory = listOf(memory),
			sessionResidue = residue
		)

		val failure = runCatching { dataSource.clearSessionResidue() }.exceptionOrNull()

		assertEquals("first failed", failure?.message)
		assertEquals(listOf(1, 1, 1), residue.map(RecordingSessionResidue::clearCount))
		assertEquals(0, memory.clearCount)
		assertEquals(false, settingsRepository.cleared)
	}

	private fun dataSource(
		persistenceMaintenanceRepository: PersistenceMaintenanceRepository,
		settingsRepository: FakeSettingsRepository,
		sessionMemory: List<SessionMemory> = emptyList(),
		sessionResidue: List<SessionResidue> = emptyList()
	) = IosApplicationDataSource(
		persistenceMaintenanceRepository = persistenceMaintenanceRepository,
		settingsRepository = settingsRepository,
		secureStore = NoOpSecureKeyValueDataRepository(),
		legacySecureStore = NoOpSecureKeyValueDataRepository(),
		attestationCapability = NoOpAttestationCapability(),
		externalActionsCapability = NoOpExternalActionsCapability(),
		sessionMemory = { sessionMemory },
		sessionResidue = { sessionResidue }
	)
}

private class RecordingSessionResidue(
	private val failure: Throwable? = null
) : SessionResidue {
	var clearCount = 0
		private set

	override suspend fun clearSessionResidue() {
		clearCount += 1
		failure?.let { throw it }
	}
}

private class RecordingSessionMemory(
	private val settingsRepository: FakeSettingsRepository,
	private val failure: Throwable? = null
) : SessionMemory {
	var clearCount = 0
		private set
	var clearedAfterTheStores = false
		private set

	override suspend fun clearSessionMemory() {
		clearCount += 1
		clearedAfterTheStores = settingsRepository.cleared
		failure?.let { throw it }
	}
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
