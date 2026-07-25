package com.gdavidpb.tuindice.data.source.cache

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.summary.data.repository.user.LocalDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CoreCacheStateDataSourceTest {
	@Test
	fun requiresBaseRehydration_returnsTrue_whenUserIsMissing() = runTest {
		val dataSource = CoreCacheStateDataSource(
			userLocalDataSource = FakeUserLocalDataRepository(user = null),
			recordLocalDataSource = FakeAcademicRecordLocalDataRepository(
				record = DEFAULT_RECORD,
				hasSyncedRecord = true
			)
		)

		assertEquals(true, dataSource.requiresBaseRehydration())
	}

	@Test
	fun requiresBaseRehydration_returnsTrue_whenRecordIsMissing() = runTest {
		val dataSource = CoreCacheStateDataSource(
			userLocalDataSource = FakeUserLocalDataRepository(user = DEFAULT_USER),
			recordLocalDataSource = FakeAcademicRecordLocalDataRepository(
				record = null,
				hasSyncedRecord = true
			)
		)

		assertEquals(true, dataSource.requiresBaseRehydration())
	}

	@Test
	fun requiresBaseRehydration_returnsTrue_whenRecordHasNeverSynced() = runTest {
		val dataSource = CoreCacheStateDataSource(
			userLocalDataSource = FakeUserLocalDataRepository(user = DEFAULT_USER),
			recordLocalDataSource = FakeAcademicRecordLocalDataRepository(
				record = DEFAULT_RECORD,
				hasSyncedRecord = false
			)
		)

		assertEquals(true, dataSource.requiresBaseRehydration())
	}

	@Test
	fun requiresBaseRehydration_returnsFalse_whenBaseCacheIsUsable() = runTest {
		val dataSource = CoreCacheStateDataSource(
			userLocalDataSource = FakeUserLocalDataRepository(user = DEFAULT_USER),
			recordLocalDataSource = FakeAcademicRecordLocalDataRepository(
				record = DEFAULT_RECORD,
				hasSyncedRecord = true
			)
		)

		assertEquals(false, dataSource.requiresBaseRehydration())
	}
}

private class FakeUserLocalDataRepository(
	private val user: User?
) : LocalDataRepository {
	override fun getUserFlow(): Flow<User?> = flowOf(user)

	override suspend fun updateUser(user: User) = Unit
}

private class FakeAcademicRecordLocalDataRepository(
	private val record: AcademicRecord?,
	private val hasSyncedRecord: Boolean
) : AcademicRecordLocalDataRepository {
	override fun observeAcademicRecordFlow(): Flow<AcademicRecord?> = flowOf(record)

	override fun observeHasSyncedRecordFlow(): Flow<Boolean> = flowOf(hasSyncedRecord)

	override suspend fun hasAcademicRecord(): Boolean = record != null

	override suspend fun getAcademicRecord(): AcademicRecord? = record

	override suspend fun getRecordRevision(): Long? = null

	override suspend fun saveAcademicRecord(record: VersionedAcademicRecord) = Unit
}

private val DEFAULT_USER = User(
	id = "user-1",
	cid = "cid-1",
	usbId = "20261234",
	email = "ana@tuindice.app",
	pictureUrl = "",
	fullName = "Ana Maria Diaz Soto",
	firstNames = "Ana Maria",
	lastNames = "Diaz Soto",
	careerName = "Ingenieria Civil Informatica",
	careerCode = 14056,
	scholarship = false,
	grade = 4.4138,
	enrolledSubjects = 5,
	enrolledCredits = 24,
	approvedSubjects = 32,
	approvedCredits = 156,
	retiredSubjects = 1,
	retiredCredits = 4,
	failedSubjects = 2,
	failedCredits = 8,
	lastUpdate = 1_709_251_200_000L
)

private val DEFAULT_RECORD = AcademicRecord(
	id = "record-1",
	terms = emptyList(),
	attemptOverrides = emptyList()
)
