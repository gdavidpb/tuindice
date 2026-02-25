package com.gdavidpb.tuindice.record.data.repository.quarter

import com.gdavidpb.tuindice.record.FakeLocalDataSource
import com.gdavidpb.tuindice.record.FakeRemoteDataSource
import com.gdavidpb.tuindice.record.FakeSettingsDataSource
import com.gdavidpb.tuindice.record.RecordFixtures
import com.gdavidpb.tuindice.record.data.repository.quarter.model.SetSubjectGradeResult
import com.gdavidpb.tuindice.record.data.repository.quarter.source.api.mapper.toRemoteQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toLocalQuarter
import com.gdavidpb.tuindice.record.data.repository.quarter.source.database.mapper.toQuarter
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QuarterDataRepositoryTest {
	private lateinit var localDataSource: FakeLocalDataSource
	private lateinit var remoteDataSource: FakeRemoteDataSource
	private lateinit var settingsDataSource: FakeSettingsDataSource
	private lateinit var repository: QuarterDataRepository

	@Before
	fun setUp() {
		localDataSource = FakeLocalDataSource()
		remoteDataSource = FakeRemoteDataSource()
		settingsDataSource = FakeSettingsDataSource()
		repository = QuarterDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource
		)
	}

	@Test
	fun getQuartersFlow_whenNotOnCooldown_fetchesRemoteAndCachesLocally() = runBlocking {
		settingsDataSource.isOnCooldown = false
		val remoteQuarters = listOf(
			RecordFixtures.remoteQuarter(
				id = "q1",
				subjects = listOf(RecordFixtures.remoteSubject(id = "s1", quarterId = "q1"))
			)
		)
		remoteDataSource.quarters = remoteQuarters

		val result = repository.getQuartersFlow().first()

		val expectedLocalQuarters = remoteQuarters.map { it.toLocalQuarter() }
		val expectedQuarters = expectedLocalQuarters.map { it.toQuarter() }

		assertEquals(expectedQuarters, result)
		assertEquals(1, remoteDataSource.getQuartersCalls)
		assertEquals(listOf(expectedLocalQuarters), localDataSource.savedQuartersCalls)
		assertEquals(1, settingsDataSource.setCooldownCalls)
	}

	@Test
	fun getQuartersFlow_whenOnCooldown_returnsLocalDataWithoutSyncing() = runBlocking {
		settingsDataSource.isOnCooldown = true
		val localQuarters = listOf(
			RecordFixtures.localQuarter(
				id = "q1",
				subjects = listOf(RecordFixtures.localSubject(id = "s1", quarterId = "q1"))
			)
		)
		localDataSource.quartersFlow.value = localQuarters

		val result = repository.getQuartersFlow().first()

		assertEquals(localQuarters.map { it.toQuarter() }, result)
		assertEquals(0, remoteDataSource.getQuartersCalls)
		assertTrue(localDataSource.savedQuartersCalls.isEmpty())
		assertEquals(0, settingsDataSource.setCooldownCalls)
	}

	@Test
	fun getQuarters_returnsFirstElementFromFlow() = runBlocking {
		settingsDataSource.isOnCooldown = true
		val localQuarters = listOf(
			RecordFixtures.localQuarter(
				id = "q1",
				subjects = listOf(RecordFixtures.localSubject(id = "s1", quarterId = "q1"))
			)
		)
		localDataSource.quartersFlow.value = localQuarters

		val result = repository.getQuarters()

		assertEquals(localQuarters.map { it.toQuarter() }, result)
	}

	@Test
	fun removeQuarter_dispatchesLocalThenRemote() = runBlocking {
		val callOrder = mutableListOf<String>()
		val localDataSource = FakeLocalDataSource(eventsSink = callOrder)
		val remoteDataSource = FakeRemoteDataSource(eventsSink = callOrder)
		val repository = QuarterDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource
		)

		repository.removeQuarter(QuarterRemove(id = "q1"))

		assertEquals(listOf("q1"), localDataSource.removedQuarterIds)
		assertEquals(listOf("q1"), remoteDataSource.removedQuarterIds)
		assertEquals(listOf("local.remove:q1", "remote.remove:q1"), callOrder)
	}

	@Test
	fun setSubjectGrade_whenDispatchDisabled_updatesLocalOnly() = runBlocking {
		val updatedQuarters = listOf(
			RecordFixtures.localQuarter(
				id = "q1",
				subjects = listOf(
					RecordFixtures.localSubject(
						id = "s1",
						quarterId = "q1",
						grade = 4
					)
				)
			)
		)
		localDataSource.setSubjectGradeAndRecomputeResult = SetSubjectGradeResult(
			updatedQuarters = updatedQuarters,
			updatedTargetQuarter = updatedQuarters.first()
		)

		repository.setSubjectGrade(
			set = SubjectGradeSet(
				id = "s1",
				quarterId = "q1",
				grade = 4,
				commit = false
			)
		)

		assertEquals(1, localDataSource.setSubjectGradeAndRecomputeCalls.size)
		assertEquals(false, localDataSource.setSubjectGradeAndRecomputeCalls.first().commit)
		assertTrue(remoteDataSource.addQuarterCalls.isEmpty())
	}

	@Test
	fun setSubjectGrade_whenDispatchEnabled_updatesRemoteWithUpdatedQuarter() = runBlocking {
		val updatedQuarters = listOf(
			RecordFixtures.localQuarter(
				id = "q1",
				subjects = listOf(
					RecordFixtures.localSubject(
						id = "s-target",
						quarterId = "q1",
						grade = 5
					)
				)
			),
			RecordFixtures.localQuarter(
				id = "q2",
				subjects = listOf(
					RecordFixtures.localSubject(
						id = "s-2",
						quarterId = "q2",
						grade = 3
					)
				)
			)
		)
		localDataSource.setSubjectGradeAndRecomputeResult = SetSubjectGradeResult(
			updatedQuarters = updatedQuarters,
			updatedTargetQuarter = updatedQuarters.first()
		)

		repository.setSubjectGrade(
			set = SubjectGradeSet(
				id = "s-target",
				quarterId = "q1",
				grade = 5,
				commit = true
			)
		)

		val expectedRemoteQuarter = updatedQuarters
			.first()
			.toQuarter()
			.toRemoteQuarter()

		assertEquals(1, localDataSource.setSubjectGradeAndRecomputeCalls.size)
		assertEquals(true, localDataSource.setSubjectGradeAndRecomputeCalls.first().commit)
		assertEquals(listOf(expectedRemoteQuarter), remoteDataSource.addQuarterCalls)
	}

	@Test
	fun setSubjectGrade_whenDispatchEnabledWithoutMatchingSubject_doesNotCallRemote() =
		runBlocking {
			localDataSource.setSubjectGradeAndRecomputeResult = SetSubjectGradeResult(
				updatedQuarters = emptyList(),
				updatedTargetQuarter = null
			)

			repository.setSubjectGrade(
				set = SubjectGradeSet(
					id = "target-subject",
					quarterId = "q1",
					grade = 5,
					commit = true
				)
			)

			assertEquals(1, localDataSource.setSubjectGradeAndRecomputeCalls.size)
			assertEquals(true, localDataSource.setSubjectGradeAndRecomputeCalls.first().commit)
			assertTrue(remoteDataSource.addQuarterCalls.isEmpty())
		}
}