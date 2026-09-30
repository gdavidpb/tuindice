package com.gdavidpb.tuindice.data.source.sync

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.data.model.SyncResult
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
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
			userLocalDataSource = RecordingUserLocalDataRepository(writes)
		)

		dataSource.saveSyncResult(SyncResult(record = RECORD, user = USER))

		assertEquals(listOf("record 3", "user user-1"), writes)
	}

	@Test
	fun saveSyncResult_whenTheRecordFails_leavesTheUserUntouched() = runTest {
		val writes = mutableListOf<String>()
		val dataSource = SyncResultLocalDataSource(
			recordLocalDataSource = RecordingRecordLocalDataRepository(writes, failure = IllegalStateException("disk full")),
			userLocalDataSource = RecordingUserLocalDataRepository(writes)
		)

		assertFailsWith<IllegalStateException> {
			dataSource.saveSyncResult(SyncResult(record = RECORD, user = USER))
		}

		assertEquals(emptyList(), writes)
	}
}

private class RecordingRecordLocalDataRepository(
	private val writes: MutableList<String>,
	private val failure: Throwable? = null
) : AcademicRecordLocalDataRepository {
	override fun observeAcademicRecordFlow(): Flow<AcademicRecord?> = flowOf(null)

	override fun observeHasSyncedRecordFlow(): Flow<Boolean> = flowOf(false)

	override suspend fun hasAcademicRecord(): Boolean = false

	override suspend fun getAcademicRecord(): AcademicRecord? = null

	override suspend fun getRecordRevision(): Long? = null

	override suspend fun saveAcademicRecord(record: VersionedAcademicRecord) {
		failure?.let { throw it }
		writes += "record ${record.revision}"
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
