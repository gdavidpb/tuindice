package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.data.model.SyncResult
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordOutboxDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyncResultLocalDataSourceTest {
	@Test
	fun saveSyncResult_writesTheRecordBeforeTheUser() = runTest {
		val writes = mutableListOf<String>()
		val dataSource = SyncResultLocalDataSource(
			recordLocalDataSource = RecordingRecordLocalDataRepository(writes),
			userLocalDataSource = RecordingUserLocalDataRepository(writes),
			recordOutboxDataSource = RecordingRecordOutboxDataRepository(writes)
		)

		dataSource.saveSyncResult(SyncResult(record = RECORD, user = USER))

		assertEquals(listOf("record 3", "outbox 3", "user user-1"), writes)
	}

	@Test
	fun saveSyncResult_whenTheQueueCannotBeReconciled_stillSavesTheUser() = runTest {
		val writes = mutableListOf<String>()
		val dataSource = SyncResultLocalDataSource(
			recordLocalDataSource = RecordingRecordLocalDataRepository(writes),
			userLocalDataSource = RecordingUserLocalDataRepository(writes),
			recordOutboxDataSource = RecordingRecordOutboxDataRepository(
				writes,
				failure = IllegalStateException("outbox unavailable")
			)
		)

		dataSource.saveSyncResult(SyncResult(record = RECORD, user = USER))

		assertEquals(listOf("record 3", "user user-1"), writes)
	}

	@Test
	fun saveSyncResult_whenTheRecordFails_leavesTheUserUntouched() = runTest {
		val writes = mutableListOf<String>()
		val dataSource = SyncResultLocalDataSource(
			recordLocalDataSource = RecordingRecordLocalDataRepository(writes, failure = IllegalStateException("disk full")),
			userLocalDataSource = RecordingUserLocalDataRepository(writes),
			recordOutboxDataSource = RecordingRecordOutboxDataRepository(writes)
		)

		assertFailsWith<IllegalStateException> {
			dataSource.saveSyncResult(SyncResult(record = RECORD, user = USER))
		}

		assertEquals(emptyList(), writes)
	}

	@Test
	fun saveSyncResult_withARecordOlderThanTheStoredOne_keepsTheStoredRecordAndSkipsTheQueue() = runTest {
		val writes = mutableListOf<String>()
		val dataSource = SyncResultLocalDataSource(
			recordLocalDataSource = RecordingRecordLocalDataRepository(writes, storedRevision = 5L),
			userLocalDataSource = RecordingUserLocalDataRepository(writes),
			recordOutboxDataSource = RecordingRecordOutboxDataRepository(writes)
		)

		dataSource.saveSyncResult(SyncResult(record = RECORD.copy(revision = 4L), user = USER))

		assertEquals(listOf("user user-1"), writes)
	}

	@Test
	fun saveSyncResult_withARecordAtTheStoredRevision_replacesIt() = runTest {
		val writes = mutableListOf<String>()
		val dataSource = SyncResultLocalDataSource(
			recordLocalDataSource = RecordingRecordLocalDataRepository(writes, storedRevision = 5L),
			userLocalDataSource = RecordingUserLocalDataRepository(writes),
			recordOutboxDataSource = RecordingRecordOutboxDataRepository(writes)
		)

		dataSource.saveSyncResult(SyncResult(record = RECORD.copy(revision = 5L), user = USER))

		assertEquals(listOf("record 5", "outbox 5", "user user-1"), writes)
	}

	@Test
	fun saveSyncResult_withANewerRecord_replacesIt() = runTest {
		val writes = mutableListOf<String>()
		val dataSource = SyncResultLocalDataSource(
			recordLocalDataSource = RecordingRecordLocalDataRepository(writes, storedRevision = 5L),
			userLocalDataSource = RecordingUserLocalDataRepository(writes),
			recordOutboxDataSource = RecordingRecordOutboxDataRepository(writes)
		)

		dataSource.saveSyncResult(SyncResult(record = RECORD.copy(revision = 6L), user = USER))

		assertEquals(listOf("record 6", "outbox 6", "user user-1"), writes)
	}
}

private class RecordingRecordLocalDataRepository(
	private val writes: MutableList<String>,
	private val failure: Throwable? = null,
	private val storedRevision: Long? = null
) : AcademicRecordLocalDataRepository {
	override fun observeAcademicRecordFlow(): Flow<AcademicRecord?> = flowOf(null)

	override fun observeHasSyncedRecordFlow(): Flow<Boolean> = flowOf(false)

	override suspend fun hasAcademicRecord(): Boolean = false

	override suspend fun getAcademicRecord(): AcademicRecord? = null

	override suspend fun getRecordRevision(): Long? = storedRevision

	override suspend fun saveAcademicRecord(record: VersionedAcademicRecord) {
		failure?.let { throw it }
		writes += "record ${record.revision}"
	}

	override suspend fun replaceAcademicRecordIfNotOlder(record: VersionedAcademicRecord): Boolean {
		failure?.let { throw it }

		if (storedRevision != null && record.revision < storedRevision) return false

		writes += "record ${record.revision}"
		return true
	}
}

private class RecordingRecordOutboxDataRepository(
	private val writes: MutableList<String>,
	private val failure: Throwable? = null
) : AcademicRecordOutboxDataRepository {
	override suspend fun reconcileWithConfirmedRecord(record: VersionedAcademicRecord) {
		failure?.let { throw it }
		writes += "outbox ${record.revision}"
	}
}

private class RecordingUserLocalDataRepository(
	private val writes: MutableList<String>
) : LocalDataRepository {
	override fun getUserFlow(): Flow<User?> = flowOf(null)

	override suspend fun updateUser(user: User) {
		writes += "user ${user.id}"
	}
}

private val RECORD = VersionedAcademicRecord(revision = 3L, record = AcademicRecord(id = "user-1"))

private val USER = User(
	id = "user-1",
	cid = "12345678",
	usbId = "12-34567",
	email = "12-34567@usb.ve",
	pictureUrl = "",
	fullName = "Ada Lovelace",
	firstNames = "Ada",
	lastNames = "Lovelace",
	careerName = "Ingenieria",
	careerCode = 200,
	scholarship = false,
	grade = 4.5,
	enrolledSubjects = 0,
	enrolledCredits = 0,
	approvedSubjects = 0,
	approvedCredits = 0,
	retiredSubjects = 0,
	retiredCredits = 0,
	failedSubjects = 0,
	failedCredits = 0,
	lastUpdate = 0L
)
