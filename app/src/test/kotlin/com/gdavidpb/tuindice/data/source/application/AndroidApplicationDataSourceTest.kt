package com.gdavidpb.tuindice.data.source.application

import android.content.ContextWrapper
import com.gdavidpb.tuindice.base.data.repository.SecureKeyValueDataRepository
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.repository.SettingsRepository
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

	private fun dataSource(
		persistenceMaintenanceRepository: PersistenceMaintenanceRepository,
		settingsRepository: SettingsRepository
	) = AndroidApplicationDataSource(
		context = TestContext(),
		persistenceMaintenanceRepository = persistenceMaintenanceRepository,
		settingsRepository = settingsRepository,
		secureStore = NoOpSecureKeyValueDataRepository(),
		legacySecureStore = NoOpSecureKeyValueDataRepository(),
		proofOfPossessionCapability = NoOpProofOfPossessionCapability()
	)
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
