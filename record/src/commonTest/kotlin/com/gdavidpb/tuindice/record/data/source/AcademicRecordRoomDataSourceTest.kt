package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicAttemptOverrideDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicRecordDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicRecordSyncStateDao
import com.gdavidpb.tuindice.persistence.data.room.daos.AcademicTermDao
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicAttemptOverrideEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicRecordEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicRecordSyncStateEntity
import com.gdavidpb.tuindice.persistence.data.room.entity.AcademicTermEntity
import com.gdavidpb.tuindice.persistence.domain.repository.PersistenceTransactionRunner
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// The record host tests have no Room database, so the DAO classes are subclassed here and every
// call lands in one log, in order. The checks are on that order: the stored revision is read, and
// the record written, inside the one transaction.
class AcademicRecordRoomDataSourceTest {
	@Test
	fun replaceAcademicRecordIfNotOlder_withAnOlderRevision_keepsTheStoredRecordAndWritesNothing() = runTest {
		val log = mutableListOf<String>()
		val recordDao = RoomRecordDaoFake(log, stored = entityAt(revision = 5L))
		val dataSource = roomDataSourceWith(recordDao, log)

		val written = dataSource.replaceAcademicRecordIfNotOlder(versionedAt(revision = 4L))

		assertFalse(written)
		assertEquals(5L, recordDao.stored?.revision)
		assertEquals(listOf("begin", "getRecord", "end"), log)
	}

	@Test
	fun replaceAcademicRecordIfNotOlder_withANewerRevision_replacesTheStoredRecordInOneTransaction() = runTest {
		val log = mutableListOf<String>()
		val recordDao = RoomRecordDaoFake(log, stored = entityAt(revision = 5L))
		val dataSource = roomDataSourceWith(recordDao, log)

		val written = dataSource.replaceAcademicRecordIfNotOlder(versionedAt(revision = 6L))

		assertTrue(written)
		assertEquals(6L, recordDao.stored?.revision)
		assertEquals(listOf("begin", "getRecord"), log.take(2))
		assertTrue("upsert record 6" in log)
		assertEquals("end", log.last())
	}

	@Test
	fun replaceAcademicRecordIfNotOlder_withTheStoredRevision_replacesIt() = runTest {
		val log = mutableListOf<String>()
		val recordDao = RoomRecordDaoFake(log, stored = entityAt(revision = 5L))
		val dataSource = roomDataSourceWith(recordDao, log)

		val written = dataSource.replaceAcademicRecordIfNotOlder(versionedAt(revision = 5L))

		assertTrue(written)
		assertEquals(5L, recordDao.stored?.revision)
		assertTrue("upsert record 5" in log)
	}

	@Test
	fun replaceAcademicRecordIfNotOlder_withoutAStoredRecord_writesIt() = runTest {
		val log = mutableListOf<String>()
		val recordDao = RoomRecordDaoFake(log, stored = null)
		val dataSource = roomDataSourceWith(recordDao, log)

		val written = dataSource.replaceAcademicRecordIfNotOlder(versionedAt(revision = 3L))

		assertTrue(written)
		assertEquals(3L, recordDao.stored?.revision)
	}

	// Both writes run under the write mutex: the save waits until the replace's transaction has ended, so the two
	// never overlap, and the save is the write that lands last.
	@Test
	fun saveAcademicRecord_whileAReplaceIsInItsTransaction_waitsForItsEnd() = runTest {
		val log = mutableListOf<String>()
		val recordDao = RoomRecordDaoFake(log, stored = entityAt(revision = 5L))
		val gate = TransactionGate()
		val dataSource = roomDataSourceWith(recordDao, log, gate)

		val replace = async { dataSource.replaceAcademicRecordIfNotOlder(versionedAt(revision = 6L)) }
		gate.began.await()
		val save = launch { dataSource.saveAcademicRecord(versionedAt(revision = 7L)) }
		gate.released.complete(Unit)
		replace.await()
		save.join()

		assertEquals(7L, recordDao.stored?.revision)
		assertEquals(
			listOf("begin", "end", "begin", "end"),
			log.filter { entry -> entry == "begin" || entry == "end" },
			"the save's transaction must begin only after the replace's transaction has ended"
		)
	}
}

private fun versionedAt(revision: Long) = VersionedAcademicRecord(
	revision = revision,
	record = AcademicRecord(id = "record-1")
)

private fun entityAt(revision: Long) = AcademicRecordEntity(
	id = "record-1",
	revision = revision,
	updatedAt = 0L
)

private fun roomDataSourceWith(
	recordDao: RoomRecordDaoFake,
	log: MutableList<String>,
	gate: TransactionGate? = null
): AcademicRecordRoomDataSource = AcademicRecordRoomDataSource(
	academicRecordDao = recordDao,
	academicRecordSyncStateDao = RoomSyncStateDaoFake(log),
	academicTermDao = RoomTermDaoFake(log),
	academicAttemptDao = RoomAttemptDaoFake(log),
	academicAttemptOverrideDao = RoomAttemptOverrideDaoFake(log),
	transactionRunner = RoomRecordingTransactionRunner(log, gate)
)

private fun notUsedByTheWrite(): Nothing = error("not used by replaceAcademicRecordIfNotOlder")

private class RoomRecordDaoFake(
	private val log: MutableList<String>,
	var stored: AcademicRecordEntity?
) : AcademicRecordDao() {
	override fun observeRecordFlow(): Flow<AcademicRecordEntity?> = notUsedByTheWrite()

	override suspend fun getRecord(): AcademicRecordEntity? {
		log += "getRecord"
		return stored
	}

	override suspend fun deleteAll() {
		log += "deleteAll record"
		stored = null
	}

	override suspend fun upsertEntity(entity: AcademicRecordEntity) {
		log += "upsert record ${entity.revision}"
		stored = entity
	}

	override suspend fun upsertEntities(entities: List<AcademicRecordEntity>) {
		notUsedByTheWrite()
	}
}

private class RoomSyncStateDaoFake(private val log: MutableList<String>) : AcademicRecordSyncStateDao() {
	override fun observeSyncState(key: String): Flow<AcademicRecordSyncStateEntity?> = notUsedByTheWrite()

	override suspend fun getSyncState(key: String): AcademicRecordSyncStateEntity? = notUsedByTheWrite()

	override suspend fun deleteAll() {
		notUsedByTheWrite()
	}

	override suspend fun upsertEntity(entity: AcademicRecordSyncStateEntity) {
		log += "upsert sync state"
	}

	override suspend fun upsertEntities(entities: List<AcademicRecordSyncStateEntity>) {
		notUsedByTheWrite()
	}
}

private class RoomTermDaoFake(private val log: MutableList<String>) : AcademicTermDao() {
	override fun observeTermsFlow(): Flow<List<AcademicTermEntity>> = notUsedByTheWrite()

	override suspend fun getTerms(): List<AcademicTermEntity> = notUsedByTheWrite()

	override suspend fun deleteAll() {
		log += "deleteAll terms"
	}

	override suspend fun upsertEntity(entity: AcademicTermEntity) {
		notUsedByTheWrite()
	}

	override suspend fun upsertEntities(entities: List<AcademicTermEntity>) {
		log += "upsert terms ${entities.size}"
	}
}

private class RoomAttemptDaoFake(private val log: MutableList<String>) : AcademicAttemptDao() {
	override fun observeAttemptsFlow(): Flow<List<AcademicAttemptEntity>> = notUsedByTheWrite()

	override suspend fun getAttempts(): List<AcademicAttemptEntity> = notUsedByTheWrite()

	override suspend fun deleteAll() {
		log += "deleteAll attempts"
	}

	override suspend fun upsertEntity(entity: AcademicAttemptEntity) {
		notUsedByTheWrite()
	}

	override suspend fun upsertEntities(entities: List<AcademicAttemptEntity>) {
		log += "upsert attempts ${entities.size}"
	}
}

private class RoomAttemptOverrideDaoFake(private val log: MutableList<String>) : AcademicAttemptOverrideDao() {
	override fun observeOverridesFlow(): Flow<List<AcademicAttemptOverrideEntity>> = notUsedByTheWrite()

	override suspend fun getOverrides(): List<AcademicAttemptOverrideEntity> = notUsedByTheWrite()

	override suspend fun deleteAll() {
		log += "deleteAll overrides"
	}

	override suspend fun upsertEntity(entity: AcademicAttemptOverrideEntity) {
		notUsedByTheWrite()
	}

	override suspend fun upsertEntities(entities: List<AcademicAttemptOverrideEntity>) {
		log += "upsert overrides ${entities.size}"
	}
}

// Every transaction the runner opens reports that it began, then waits until the test releases the gate.
private class TransactionGate {
	val began = CompletableDeferred<Unit>()
	val released = CompletableDeferred<Unit>()
}

private class RoomRecordingTransactionRunner(
	private val log: MutableList<String>,
	private val gate: TransactionGate? = null
) : PersistenceTransactionRunner {
	override suspend fun <R> immediate(block: suspend () -> R): R {
		log += "begin"
		gate?.let { held ->
			held.began.complete(Unit)
			held.released.await()
		}
		val result = block()
		log += "end"
		return result
	}

	override suspend fun <R> read(block: suspend () -> R): R = notUsedByTheWrite()
}
