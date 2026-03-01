package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.record.data.repository.quarter.mapper.toQuarter
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_LOCAL_QUARTER
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_REMOTE_QUARTER
import com.gdavidpb.tuindice.record.testing.FakeQuarterLocalDataSource
import com.gdavidpb.tuindice.record.testing.FakeQuarterRemoteDataSource
import com.gdavidpb.tuindice.record.testing.FakeQuarterSettingsDataSource
import com.gdavidpb.tuindice.record.testing.UPDATED_RECORD_LOCAL_QUARTER
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
	fun removeQuarter_deletesLocallyAndRemotely() = runTest {
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
	fun setSubjectGrade_whenCommitted_pushesUpdatedQuarterToRemote() = runTest {
		val localDataSource = FakeQuarterLocalDataSource(
			setSubjectGradeResult = com.gdavidpb.tuindice.record.data.repository.quarter.model.SetSubjectGradeResult(
				updatedQuarters = listOf(UPDATED_RECORD_LOCAL_QUARTER),
				updatedTargetQuarter = UPDATED_RECORD_LOCAL_QUARTER
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
				commit = true
			)
		)

		assertEquals(
			DEFAULT_RECORD_QUARTER.id,
			localDataSource.lastSetSubjectGradeArgs?.quarterId
		)
		assertEquals(85, localDataSource.lastSetSubjectGradeArgs?.grade)
		assertEquals(UPDATED_RECORD_LOCAL_QUARTER.toQuarter().grade, remoteDataSource.addedQuarters.single().grade)
		assertEquals(85, remoteDataSource.addedQuarters.single().subjects.single().grade)
	}
}
