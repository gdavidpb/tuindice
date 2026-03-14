package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.record.data.repository.quarter.mapper.toLocalQuarter
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_LOCAL_QUARTER
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_REMOTE_QUARTER
import com.gdavidpb.tuindice.record.testing.FakeQuarterLocalDataSource
import com.gdavidpb.tuindice.record.testing.FakeQuarterRemoteDataSource
import com.gdavidpb.tuindice.record.testing.FakeQuarterSettingsDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QuarterRepositoryContractTest {
	@Test
	fun getQuartersFlow_refreshesLocalCache_whenCooldownIsDisabled() = runTest {
		val localDataSource = FakeQuarterLocalDataSource(initialQuarters = emptyList())
		val remoteDataSource = FakeQuarterRemoteDataSource(
			quarters = listOf(DEFAULT_RECORD_REMOTE_QUARTER)
		)
		val settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = false)
		val repository = QuarterDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource
		)

		val quarters = repository.getQuartersFlow().first()

		assertEquals(listOf(DEFAULT_RECORD_QUARTER), quarters)
		assertEquals(listOf(listOf(DEFAULT_RECORD_LOCAL_QUARTER)), localDataSource.savedQuarters)
		assertEquals(1, remoteDataSource.getQuartersCalls)
		assertTrue(settingsDataSource.cooldownMarked)
	}

	@Test
	fun removeQuarter_deletesLocallyAfterRemoteSucceeds() = runTest {
		val localDataSource = FakeQuarterLocalDataSource()
		val remoteDataSource = FakeQuarterRemoteDataSource()
		val repository = QuarterDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = true)
		)

		repository.removeQuarter(QuarterRemove(id = DEFAULT_RECORD_QUARTER.id))

		assertEquals(listOf(DEFAULT_RECORD_QUARTER.id), localDataSource.removedQuarterIds)
		assertEquals(listOf(DEFAULT_RECORD_QUARTER.id), remoteDataSource.removedQuarterIds)
	}

	@Test
	fun removeQuarter_keepsLocalStateWhenRemoteFails() = runTest {
		val localDataSource = FakeQuarterLocalDataSource()
		val remoteDataSource = FakeQuarterRemoteDataSource(
			removeQuarterThrowable = IllegalStateException("boom")
		)
		val repository = QuarterDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = true)
		)

		assertFailsWith<IllegalStateException> {
			repository.removeQuarter(QuarterRemove(id = DEFAULT_RECORD_QUARTER.id))
		}

		assertEquals(emptyList(), localDataSource.removedQuarterIds)
		assertEquals(emptyList(), remoteDataSource.removedQuarterIds)
	}

	@Test
	fun setSubjectGrade_whenPreviewOnly_updatesLocalPreviewWithoutCallingRemote() = runTest {
		val localDataSource = FakeQuarterLocalDataSource(
			setSubjectGradeResult = com.gdavidpb.tuindice.record.data.repository.quarter.model.SetSubjectGradeResult(
				updatedQuarters = listOf(DEFAULT_RECORD_LOCAL_QUARTER),
				updatedTargetQuarter = DEFAULT_RECORD_LOCAL_QUARTER
			)
		)
		val remoteDataSource = FakeQuarterRemoteDataSource()
		val repository = QuarterDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = true)
		)

		repository.setSubjectGrade(
			SubjectGradeSet(
				quarterId = DEFAULT_RECORD_QUARTER.id,
				id = DEFAULT_RECORD_QUARTER.subjects.single().id,
				grade = 85,
				commit = false
			)
		)

		assertEquals(
			DEFAULT_RECORD_QUARTER.id,
			localDataSource.lastSetSubjectGradeArgs?.quarterId
		)
		assertEquals(85, localDataSource.lastSetSubjectGradeArgs?.grade)
			assertEquals(false, localDataSource.lastSetSubjectGradeArgs?.commit)
			assertEquals(emptyList(), remoteDataSource.setSubjectGradeCalls)
			assertEquals(emptyList(), localDataSource.savedQuarters)
	}

	@Test
	fun setSubjectGrade_whenCommitted_replacesLocalSnapshotWithRemoteResponse() = runTest {
		val localDataSource = FakeQuarterLocalDataSource()
		val remoteDataSource = FakeQuarterRemoteDataSource(
			quarters = listOf(DEFAULT_RECORD_REMOTE_QUARTER.copy())
		)
		val repository = QuarterDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = true)
		)

		repository.setSubjectGrade(
			SubjectGradeSet(
				quarterId = DEFAULT_RECORD_QUARTER.id,
				id = DEFAULT_RECORD_QUARTER.subjects.single().id,
				grade = 85,
				commit = true
			)
		)

		assertEquals(emptyList(), localDataSource.removedQuarterIds)
		assertEquals(null, localDataSource.lastSetSubjectGradeArgs)
		assertEquals(85, remoteDataSource.setSubjectGradeCalls.single().grade)
		assertEquals(
			listOf(DEFAULT_RECORD_REMOTE_QUARTER.toLocalQuarter()),
			localDataSource.savedQuarters.single()
		)
	}

	@Test
	fun setSubjectGrade_whenCommittedAndRemoteFails_clearsPreviewAndRethrows() = runTest {
		val localDataSource = FakeQuarterLocalDataSource()
		val remoteDataSource = FakeQuarterRemoteDataSource(
			setSubjectGradeThrowable = IllegalStateException("boom")
		)
		val repository = QuarterDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = true)
		)

		assertFailsWith<IllegalStateException> {
			repository.setSubjectGrade(
				SubjectGradeSet(
					quarterId = DEFAULT_RECORD_QUARTER.id,
					id = DEFAULT_RECORD_QUARTER.subjects.single().id,
					grade = 85,
					commit = true
				)
			)
		}

		assertEquals(
			listOf(DEFAULT_RECORD_QUARTER.id to DEFAULT_RECORD_QUARTER.subjects.single().id),
			localDataSource.clearedPreviewArgs
		)
	}
}
