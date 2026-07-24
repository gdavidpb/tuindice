package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.record.data.mutation.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.mutation.RECORD_MUTATION_SCOPE
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `record` escribe la base local de inmediato y reaplica los sobres del outbox sobre
 * cada snapshot remoto. Si esa reaplicacion solo mira los `Pending`, un fallo terminal
 * revierte en silencio un cambio que el usuario ya veia aplicado.
 */
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
		// El snapshot remoto no conoce el cambio: nunca llego a enviarse.
		val remoteDataSource = ControlledAcademicRecordRemoteDataRepository(
			upsertResponse = defaultVersionedRecord(revision = 2L)
		)
		val dataSource = AcademicRecordDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeRecordSettingsDataRepository(),
			mutationEngine = createMutationEngine(
				coroutineScope = this,
				outboxStore = InMemoryMutationEnvelopeStore(listOf(failedMutation))
			),
			identifierRepository = FakeIdentifierRepository()
		)

		dataSource.updateAcademicRecord(forceRemote = true)

		val overrides = requireNotNull(localDataSource.getAcademicRecord()).attemptOverrides
		assertEquals(listOf(attemptId), overrides.map(AttemptOverride::attemptId))
		assertEquals(AttemptScore.numeric(5), overrides.single().score)
	}
}
