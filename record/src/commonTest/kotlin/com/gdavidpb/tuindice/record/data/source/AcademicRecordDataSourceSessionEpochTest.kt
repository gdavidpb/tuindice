package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// The record has the same fence as the evaluations, the pensum and the queue: a download that lands
// after the session ended leaves no trace, and the end of the session never waits for a request.
@OptIn(ExperimentalCoroutinesApi::class)
class AcademicRecordDataSourceSessionEpochTest {
	@Test
	fun updateAcademicRecord_whenTheDownloadLandsAfterTheSessionEnded_savesNothingAndMarksNoCooldown() = runTest {
		val remote = HeldRecordRemote(ControlledAcademicRecordRemoteDataRepository(defaultVersionedRecord(revision = 2L)))
		val local = FakeAcademicRecordLocalDataRepository(record = null, hasSyncedRecord = false)
		val settings = FakeRecordSettingsDataRepository()
		val dataSource = dataSource(remote = remote, local = local, settings = settings)

		val update = launch { dataSource.updateAcademicRecord() }
		runCurrent()
		assertTrue(remote.getRecordStarted.isCompleted, "the download is in flight")
		dataSource.clearSessionMemory()
		remote.release()
		runCurrent()

		assertTrue(update.isCompleted)
		assertEquals(emptyList(), local.savedRecords, "the previous account's record is not stored")
		assertFalse(settings.cooldownMarked, "and it marks no cooldown for the next one")
	}

	@Test
	fun updateAcademicRecord_whenTheSessionDidNotEnd_savesAndMarksTheCooldown() = runTest {
		val remote = HeldRecordRemote(ControlledAcademicRecordRemoteDataRepository(defaultVersionedRecord(revision = 2L)))
		val local = FakeAcademicRecordLocalDataRepository(record = null, hasSyncedRecord = false)
		val settings = FakeRecordSettingsDataRepository()
		val dataSource = dataSource(remote = remote, local = local, settings = settings)

		val update = launch { dataSource.updateAcademicRecord() }
		runCurrent()
		remote.release()
		runCurrent()

		assertTrue(update.isCompleted)
		assertEquals(1, local.savedRecords.size)
		assertTrue(settings.cooldownMarked)
	}

	// The snapshot a 409 asks for while the queue resolves a failure is saved before the engine
	// checks its own epoch, so the data source fences it itself.
	@Test
	fun theSnapshotAConflictDownloads_afterTheSessionEnded_isNotStored() = runTest {
		val delegate = ControlledAcademicRecordRemoteDataRepository(defaultVersionedRecord(revision = 2L)).apply {
			deleteSyntheticTermThrowable = clientRequestException(HttpStatusCode.Conflict)
		}
		val remote = HeldRecordRemote(delegate)
		val local = FakeAcademicRecordLocalDataRepository(record = recordWithASyntheticTerm())
		val engine = createMutationEngine(this)
		val dataSource = dataSource(remote = remote, local = local, engine = engine)

		val deletion = launch { dataSource.deleteSyntheticTerm("2026-JUL_AUG") }
		runCurrent()
		assertTrue(remote.getRecordStarted.isCompleted, "the conflict asked for the download")
		// The real wipe tells every holder; the queue retrying for the account that left is its own fence.
		dataSource.clearSessionMemory()
		engine.clearSessionMemory()
		remote.release()
		runCurrent()

		assertTrue(deletion.isCompleted)
		assertEquals(emptyList(), local.savedRecords, "the previous account's snapshot is not stored")
	}

	@Test
	fun clearSessionMemory_doesNotWaitForARequest() = runTest {
		val remote = HeldRecordRemote(ControlledAcademicRecordRemoteDataRepository(defaultVersionedRecord(revision = 2L)))
		val dataSource = dataSource(remote = remote)

		val update = launch { dataSource.updateAcademicRecord() }
		runCurrent()
		assertTrue(remote.getRecordStarted.isCompleted)

		// Returns at once: the request in flight does not hold what the end of the session needs.
		dataSource.clearSessionMemory()

		update.cancel()
	}

	// A save that already began finishes before the session is told it ended.
	@Test
	fun clearSessionMemory_waitsForASaveInProgress() = runTest {
		val remote = HeldRecordRemote(ControlledAcademicRecordRemoteDataRepository(defaultVersionedRecord(revision = 2L)))
		remote.release()
		val local = HeldSaveRecordLocal(FakeAcademicRecordLocalDataRepository(record = null, hasSyncedRecord = false))
		val dataSource = dataSource(remote = remote, local = local)

		val update = launch { dataSource.updateAcademicRecord() }
		runCurrent()
		assertTrue(local.saveStarted, "the answer is being saved")

		var cleared = false
		launch {
			dataSource.clearSessionMemory()
			cleared = true
		}
		runCurrent()
		assertFalse(cleared, "the clearing waits for the local write that already began")

		local.saveGate.complete(Unit)
		runCurrent()

		assertTrue(cleared)
		assertTrue(update.isCompleted)
		assertEquals(1, local.delegate.savedRecords.size, "the save ended before the session did")
	}

	private fun TestScope.dataSource(
		remote: AcademicRecordRemoteDataRepository,
		local: AcademicRecordLocalDataRepository = FakeAcademicRecordLocalDataRepository(record = null),
		settings: FakeRecordSettingsDataRepository = FakeRecordSettingsDataRepository(),
		engine: StoreBackedMutationEngine<String, AcademicRecordMutation, VersionedAcademicRecord> =
			createMutationEngine(this)
	) = AcademicRecordDataSource(
		localDataSource = local,
		remoteDataSource = remote,
		settingsDataSource = settings,
		mutationEngine = engine,
		identifierRepository = FakeIdentifierRepository()
	)

	private fun recordWithASyntheticTerm() = VersionedAcademicRecord(
		revision = 12L,
		record = AcademicRecord(
			id = "record-1",
			terms = listOf(
				AcademicTerm(
					id = "2026-JUL_AUG",
					periodYear = 2026,
					periodCode = AcademicTermPeriod.JUL_AUG,
					kind = TermKind.SYNTHETIC
				)
			)
		)
	)
}

// Holds every download of the record until released.
private class HeldRecordRemote(
	private val delegate: ControlledAcademicRecordRemoteDataRepository
) : AcademicRecordRemoteDataRepository by delegate {
	val getRecordStarted = CompletableDeferred<Unit>()
	private val gate = CompletableDeferred<Unit>()

	fun release() {
		gate.complete(Unit)
	}

	override suspend fun getAcademicRecord(): VersionedAcademicRecord {
		getRecordStarted.complete(Unit)
		gate.await()
		return delegate.getAcademicRecord()
	}
}

// Holds the save of the record until told, recording that it began.
private class HeldSaveRecordLocal(
	val delegate: FakeAcademicRecordLocalDataRepository
) : AcademicRecordLocalDataRepository by delegate {
	val saveGate = CompletableDeferred<Unit>()
	var saveStarted = false
		private set

	override suspend fun saveAcademicRecord(record: VersionedAcademicRecord) {
		saveStarted = true
		saveGate.await()
		delegate.saveAcademicRecord(record)
	}
}
