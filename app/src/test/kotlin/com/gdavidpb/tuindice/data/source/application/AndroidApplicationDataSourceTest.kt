package com.gdavidpb.tuindice.data.source.application

import android.content.ContextWrapper
import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
import com.gdavidpb.tuindice.base.domain.session.SessionMemory
import com.gdavidpb.tuindice.base.domain.session.SessionResidue
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceMaintenanceRepository
import com.gdavidpb.tuindice.platform.android.AndroidProofOfPossessionCapability
import com.gdavidpb.tuindice.security.data.model.AttestationProofOfPossessionRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AndroidApplicationDataSourceTest {
	@Test
	fun clearData_whenDatabaseWipeFails_keepsTheLocalDataOwnerMarker() = runTest {
		val settingsRepository = RecordingSettingsRepository(localDataOwner = "20-26123")
		val dataSource = dataSource(
			persistenceMaintenanceRepository = FailingPersistenceMaintenanceRepository(),
			settingsRepository = settingsRepository
		)

		val failure = runCatching { dataSource.clearData() }.exceptionOrNull()

		assertTrue(failure is IllegalStateException)
		assertEquals("20-26123", settingsRepository.getLocalDataOwner())
		assertEquals(false, settingsRepository.cleared)
	}

	@Test
	fun clearData_whenEverythingSucceeds_dropsTheLocalDataOwnerMarker() = runTest {
		val settingsRepository = RecordingSettingsRepository(localDataOwner = "20-26123")
		val dataSource = dataSource(
			persistenceMaintenanceRepository = RecordingPersistenceMaintenanceRepository(),
			settingsRepository = settingsRepository
		)

		dataSource.clearData()

		assertNull(settingsRepository.getLocalDataOwner())
		assertEquals(true, settingsRepository.cleared)
	}

	@Test
	fun clearData_whenEverythingSucceeds_clearsEverySessionMemoryAfterTheStores() = runTest {
		val settingsRepository = RecordingSettingsRepository(localDataOwner = "20-26123")
		val holders = listOf(
			RecordingSessionMemory(settingsRepository),
			RecordingSessionMemory(settingsRepository)
		)
		val dataSource = dataSource(
			persistenceMaintenanceRepository = RecordingPersistenceMaintenanceRepository(),
			settingsRepository = settingsRepository,
			sessionMemory = holders
		)

		dataSource.clearData()

		assertEquals(listOf(true, true), holders.map(RecordingSessionMemory::clearedAfterTheStores))
	}

	@Test
	fun clearData_whenDatabaseWipeFails_stillClearsEverySessionMemory() = runTest {
		val settingsRepository = RecordingSettingsRepository(localDataOwner = "20-26123")
		val holders = listOf(
			RecordingSessionMemory(settingsRepository, failure = IllegalStateException("holder failed")),
			RecordingSessionMemory(settingsRepository)
		)
		val dataSource = dataSource(
			persistenceMaintenanceRepository = FailingPersistenceMaintenanceRepository(),
			settingsRepository = settingsRepository,
			sessionMemory = holders
		)

		val failure = runCatching { dataSource.clearData() }.exceptionOrNull()

		assertEquals("database wipe failed", failure?.message)
		assertEquals(listOf(1, 1), holders.map(RecordingSessionMemory::clearCount))
	}

	// Only the residue contract is asked: the stores and the session memory stay as they are.
	@Test
	fun clearSessionResidue_asksEveryHolderAndThrowsTheFirstFailureAfterwards() = runTest {
		val settingsRepository = RecordingSettingsRepository(localDataOwner = "20-26123")
		val residue = listOf(
			RecordingSessionResidue(failure = IllegalStateException("first failed")),
			RecordingSessionResidue(failure = IllegalStateException("second failed")),
			RecordingSessionResidue()
		)
		val memory = RecordingSessionMemory(settingsRepository)
		val dataSource = dataSource(
			persistenceMaintenanceRepository = RecordingPersistenceMaintenanceRepository(),
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
		settingsRepository: SettingsRepository,
		sessionMemory: List<SessionMemory> = emptyList(),
		sessionResidue: List<SessionResidue> = emptyList()
	) = AndroidApplicationDataSource(
		context = TestContext(),
		persistenceMaintenanceRepository = persistenceMaintenanceRepository,
		settingsRepository = settingsRepository,
		secureStore = NoOpSecureKeyValueDataRepository(),
		legacySecureStore = NoOpSecureKeyValueDataRepository(),
		proofOfPossessionCapability = NoOpProofOfPossessionCapability(),
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
	private val settingsRepository: RecordingSettingsRepository,
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

private class TestContext : ContextWrapper(null) {
	private val root: File = File.createTempFile("tuindice-clear-data", "").let { file ->
		file.delete()
		file.mkdirs()
		file
	}

	override fun getPackageName(): String = "com.gdavidpb.tuindice"

	override fun getFilesDir(): File = File(root, "files").apply { mkdirs() }

	override fun getCacheDir(): File = File(root, "cache").apply { mkdirs() }

	override fun getNoBackupFilesDir(): File = File(root, "no-backup").apply { mkdirs() }

	override fun getCodeCacheDir(): File = File(root, "code-cache").apply { mkdirs() }
}

private class FailingPersistenceMaintenanceRepository : PersistenceMaintenanceRepository {
	override suspend fun clearAll(): Unit = error("database wipe failed")
}

private class RecordingPersistenceMaintenanceRepository : PersistenceMaintenanceRepository {
	override suspend fun clearAll() = Unit
}

private class NoOpSecureKeyValueDataRepository : SecureKeyValueDataRepository {
	override suspend fun getString(key: String): String? = null

	override suspend fun putString(key: String, value: String) = Unit

	override suspend fun remove(key: String) = Unit

	override suspend fun clear() = Unit
}

private class NoOpProofOfPossessionCapability : AndroidProofOfPossessionCapability {
	override suspend fun resolveProofOfPossessionKeyId(): String = "key-id"

	override suspend fun invalidateProofOfPossessionKeyId() = Unit

	override suspend fun createProofOfPossession(
		attestationInput: String,
		keyId: String,
		requireKeyAttestation: Boolean
	): AttestationProofOfPossessionRequest = error("not used")
}

private class RecordingSettingsRepository(
	private var localDataOwner: String? = null
) : SettingsRepository {
	var cleared = false
		private set

	override suspend fun isReviewSuggested(value: Int): Boolean = false

	override suspend fun getLastMainSection(): MainSection = MainSection.SUMMARY

	override suspend fun setLastMainSection(section: MainSection) = Unit

	override suspend fun getOutdatedAppState(): OutdatedAppState? = null

	override suspend fun setOutdatedAppState(state: OutdatedAppState) = Unit

	override suspend fun clearOutdatedAppState() = Unit

	override suspend fun migrateLegacyOnboardingState(completedCoachmarkIds: Set<String>) = Unit

	override suspend fun getSeenCoachmarkIds(): Set<String> = emptySet()

	override suspend fun markCoachmarkSeen(coachmarkId: String) = Unit

	override suspend fun setSessionResetNoticePending() = Unit

	override suspend fun consumeSessionResetNoticePending(): Boolean = false

	override suspend fun getLocalDataOwner(): String? = localDataOwner

	override suspend fun setLocalDataOwner(usbId: String) {
		localDataOwner = usbId
	}

	override suspend fun clear() {
		cleared = true
		localDataOwner = null
	}
}
