package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_SCOPE
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicRecordFailedMutationVisibilityTest {
	@Test
	fun updateAcademicRecord_reappliesFailedMutationsOverRemoteSnapshot() = runTest {
		val attemptId = "attempt-1"
		val failedMutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = "mutation-failed",
			scopeKey = RECORD_MUTATION_SCOPE,
			command = AcademicRecordMutation.UpsertAttemptOverride(
				attemptId = attemptId,
				score = AttemptScore.numeric(5),
				outcome = null
			),
			precondition = MutationPrecondition.Revision(1L),
			status = PendingMutationStatus.Failed,
			createdAt = 1L,
			updatedAt = 1L,
			lastError = "terminal"
		)
		val localDataSource = FakeAcademicRecordLocalDataRepository(
			record = defaultVersionedRecord(revision = 1L)
		)
		val remoteDataSource = ControlledAcademicRecordRemoteDataRepository(
			upsertResponse = defaultVersionedRecord(revision = 2L)
		)
		val dataSource = AcademicRecordDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeRecordSettingsDataRepository(),
			mutationEngine = createMutationEngine(
				coroutineScope = this,
				outboxStore = InMemoryMutationEnvelopeStore(listOf(failedMutation)),
				failedRetryBackoffMillis = Long.MAX_VALUE
			),
			identifierRepository = FakeIdentifierRepository()
		)

		dataSource.updateAcademicRecord(forceRemote = true)

		val overrides = requireNotNull(dataSource.getAcademicRecord()).attemptOverrides
		assertEquals(listOf(attemptId), overrides.map(AttemptOverride::attemptId))
		assertEquals(AttemptScore.numeric(5), overrides.single().score)
	}

	// Deliberate asymmetry with the Failed case above: Failed rows are transient (the drain
	// backoff retries them) so their edits stay visible; FailedTerminal rows were rejected
	// for good by the server, and keeping them applied would make the view lie indefinitely.
	@Test
	fun updateAcademicRecord_hidesTerminallyRejectedMutationsFromVisibleRecord() = runTest {
		val terminalMutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = "mutation-terminal",
			scopeKey = RECORD_MUTATION_SCOPE,
			command = AcademicRecordMutation.UpsertAttemptOverride(
				attemptId = "attempt-1",
				score = AttemptScore.numeric(5),
				outcome = null
			),
			precondition = MutationPrecondition.Revision(1L),
			status = PendingMutationStatus.FailedTerminal,
			createdAt = 1L,
			updatedAt = 1L,
			lastError = "rejected"
		)
		val localDataSource = FakeAcademicRecordLocalDataRepository(
			record = defaultVersionedRecord(revision = 1L)
		)
		val remoteDataSource = ControlledAcademicRecordRemoteDataRepository(
			upsertResponse = defaultVersionedRecord(revision = 2L)
		)
		val dataSource = AcademicRecordDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeRecordSettingsDataRepository(),
			mutationEngine = createMutationEngine(
				coroutineScope = this,
				outboxStore = InMemoryMutationEnvelopeStore(listOf(terminalMutation)),
				failedRetryBackoffMillis = Long.MAX_VALUE
			),
			identifierRepository = FakeIdentifierRepository()
		)

		dataSource.updateAcademicRecord(forceRemote = true)

		val overrides = requireNotNull(dataSource.getAcademicRecord()).attemptOverrides
		assertEquals(emptyList(), overrides)
	}
}
