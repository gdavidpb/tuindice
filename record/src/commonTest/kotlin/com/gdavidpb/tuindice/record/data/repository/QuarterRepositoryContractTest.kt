package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutation
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_LOCAL_QUARTER
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_LOCAL_SUBJECT
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_REMOTE_QUARTER
import com.gdavidpb.tuindice.record.testing.FakeMutationOutboxRepository
import com.gdavidpb.tuindice.record.testing.FakeQuarterLocalDataSource
import com.gdavidpb.tuindice.record.testing.FakeQuarterRemoteDataSource
import com.gdavidpb.tuindice.record.testing.FakeQuarterSettingsDataSource
import com.gdavidpb.tuindice.record.testing.SetSubjectGradeCall
import com.gdavidpb.tuindice.testkit.base.repository.FakeIdentifierRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QuarterRepositoryContractTest {
	@Test
	fun observeQuartersFlow_emitsLocalQuarters_withoutRefreshing() = runTest {
		val localDataSource = FakeQuarterLocalDataSource(
			initialQuarters = listOf(DEFAULT_RECORD_LOCAL_QUARTER)
		)
		val remoteDataSource = FakeQuarterRemoteDataSource(
			quarters = listOf(DEFAULT_RECORD_REMOTE_QUARTER)
		)
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = false)
		)

		val quarters = repository.observeQuartersFlow().first()

		assertEquals(listOf(DEFAULT_RECORD_QUARTER), quarters)
		assertEquals(0, remoteDataSource.getQuartersCalls)
		assertTrue(localDataSource.savedQuarters.isEmpty())
	}

	@Test
	fun updateQuarters_refreshesLocalCache_whenCooldownIsDisabled() = runTest {
		val localDataSource = FakeQuarterLocalDataSource(initialQuarters = emptyList())
		val remoteDataSource = FakeQuarterRemoteDataSource(
			quarters = listOf(DEFAULT_RECORD_REMOTE_QUARTER)
		)
		val settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = false)
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource
		)

		repository.updateQuarters()

		assertEquals(listOf(listOf(DEFAULT_RECORD_LOCAL_QUARTER)), localDataSource.savedQuarters)
		assertEquals(1, remoteDataSource.getQuartersCalls)
		assertTrue(settingsDataSource.cooldownMarked)
	}

	@Test
	fun setSubjectGrade_whenPreviewOnly_updatesLocalPreviewWithoutCallingRemoteOrOutbox() = runTest {
		val localDataSource = FakeQuarterLocalDataSource()
		val remoteDataSource = FakeQuarterRemoteDataSource()
		val outboxRepository = FakeMutationOutboxRepository<RecordMutation>()
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			outboxRepository = outboxRepository
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
			SetSubjectGradeCall(
				quarterId = DEFAULT_RECORD_QUARTER.id,
				subjectId = DEFAULT_RECORD_QUARTER.subjects.single().id,
				grade = 85,
				commit = false
			),
			localDataSource.lastSetSubjectGradeArgs
		)
		assertTrue(remoteDataSource.setSubjectGradeCalls.isEmpty())
		assertTrue(outboxRepository.getPendingMutations().isEmpty())
	}

	@Test
	fun setSubjectGrade_whenCommitted_enqueuesAndSendsMutationImmediately() = runTest {
		val localDataSource = FakeQuarterLocalDataSource()
		val remoteDataSource = FakeQuarterRemoteDataSource()
		val outboxRepository = FakeMutationOutboxRepository<RecordMutation>()
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			outboxRepository = outboxRepository
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
			SetSubjectGradeCall(
				quarterId = DEFAULT_RECORD_QUARTER.id,
				subjectId = DEFAULT_RECORD_QUARTER.subjects.single().id,
				grade = 85,
				commit = true
			),
			localDataSource.lastSetSubjectGradeArgs
		)
		assertEquals(1, remoteDataSource.setSubjectGradeCalls.size)
		assertEquals("mutation-1", remoteDataSource.setSubjectGradeCalls.single().mutationId)
		assertEquals(DEFAULT_RECORD_LOCAL_SUBJECT.revision, remoteDataSource.setSubjectGradeCalls.single().expectedRevision)
		assertTrue(outboxRepository.getPendingMutations().isEmpty())
		assertEquals(85, localDataSource.getQuarter(DEFAULT_RECORD_QUARTER.id)?.subjects?.single()?.grade)
	}

	@Test
	fun setSubjectGrade_whenRemoteFails_keepsPendingMutationAndRethrows() = runTest {
		val localDataSource = FakeQuarterLocalDataSource()
		val remoteDataSource = FakeQuarterRemoteDataSource(
			setSubjectGradeThrowable = IllegalStateException("boom")
		)
		val outboxRepository = FakeMutationOutboxRepository<RecordMutation>()
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			outboxRepository = outboxRepository
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

		val pending = outboxRepository.getPendingMutations().single()
		assertEquals(PendingMutationStatus.Failed, pending.status)
		assertEquals(85, localDataSource.getQuarter(DEFAULT_RECORD_QUARTER.id)?.subjects?.single()?.grade)
	}

	@Test
	fun removeQuarter_whenRemoteSucceeds_confirmsLocalRemovalAndClearsPending() = runTest {
		val localDataSource = FakeQuarterLocalDataSource()
		val remoteDataSource = FakeQuarterRemoteDataSource()
		val outboxRepository = FakeMutationOutboxRepository<RecordMutation>()
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			outboxRepository = outboxRepository
		)

		repository.removeQuarter(QuarterRemove(id = DEFAULT_RECORD_QUARTER.id))

		assertEquals(1, remoteDataSource.removeQuarterCalls.size)
		assertEquals(DEFAULT_RECORD_QUARTER.id, remoteDataSource.removeQuarterCalls.single().quarterId)
		assertEquals(listOf(DEFAULT_RECORD_QUARTER.id), localDataSource.confirmedRemovedQuarterIds)
		assertTrue(outboxRepository.getPendingMutations().isEmpty())
	}

	@Test
	fun removeQuarter_whenRemoteFails_keepsPendingDeletionAndRethrows() = runTest {
		val localDataSource = FakeQuarterLocalDataSource()
		val remoteDataSource = FakeQuarterRemoteDataSource(
			removeQuarterThrowable = IllegalStateException("boom")
		)
		val outboxRepository = FakeMutationOutboxRepository<RecordMutation>()
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			outboxRepository = outboxRepository
		)

		assertFailsWith<IllegalStateException> {
			repository.removeQuarter(QuarterRemove(id = DEFAULT_RECORD_QUARTER.id))
		}

		assertTrue(localDataSource.confirmedRemovedQuarterIds.isEmpty())
		assertEquals(1, outboxRepository.getPendingMutations().size)
	}

	private fun repository(
		localDataSource: FakeQuarterLocalDataSource,
		remoteDataSource: FakeQuarterRemoteDataSource,
		settingsDataSource: FakeQuarterSettingsDataSource = FakeQuarterSettingsDataSource(onCooldown = true),
		outboxRepository: FakeMutationOutboxRepository<RecordMutation> = FakeMutationOutboxRepository()
	): QuarterDataRepository {
		return QuarterDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource,
			mutationOutboxRepository = outboxRepository,
			identifierRepository = FakeIdentifierRepository(identifier = "mutation-1")
		)
	}
}
