package com.gdavidpb.tuindice.record.data.mutation

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureKind
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationFailureResolution
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ktor.serverResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Incident regression coverage: during a university outage a 6.4.0 client re-sent a
 * DELETE that kept 404ing because the failure resolution depended on a refresh that
 * failed inside the same degraded window. Non-retryable outcomes must resolve even
 * when the reconciliation refresh cannot.
 */
class AcademicRecordMutationSyncSpecResolveFailureTest {
	@Test
	fun resolveFailure_whenOffline_defersWithoutCallingRefresh() = runTest {
		var refreshCalls = 0
		val spec = specUnderTest(refreshRemoteSnapshot = {
			refreshCalls++
			error("not reachable")
		})

		val resolution = spec.resolveFailure(
			mutation = deleteSyntheticTermEnvelope(),
			throwable = IllegalStateException("Could not connect to the server.")
		)

		assertIs<MutationFailureResolution.Defer<String, AcademicRecordMutation>>(resolution)
		assertEquals(0, refreshCalls)
	}

	// The safe refresh swallows failures on purpose, but a cancelled scope is not a failed
	// refresh: swallowing it would resolve the mutation from inside a dead pass.
	@Test
	fun resolveFailure_whenRefreshIsCancelled_propagatesCancellationInsteadOfResolving() = runTest {
		val spec = specUnderTest(refreshRemoteSnapshot = {
			throw CancellationException("scope cancelled")
		})

		assertFailsWith<CancellationException> {
			spec.resolveFailure(
				mutation = deleteSyntheticTermEnvelope(),
				throwable = clientRequestException(HttpStatusCode.NotFound)
			)
		}
	}

	@Test
	fun classifyError_mapsConflictPreconditionNotFoundAndTerminal() {
		val spec = specUnderTest()
		val mutation = deleteSyntheticTermEnvelope()

		assertEquals(
			MutationFailureKind.Conflict,
			spec.classifyError(mutation, clientRequestException(HttpStatusCode.Conflict))
		)
		assertEquals(
			MutationFailureKind.PreconditionFailed,
			spec.classifyError(mutation, clientRequestException(HttpStatusCode.PreconditionFailed))
		)
		assertEquals(
			MutationFailureKind.NotFound,
			spec.classifyError(mutation, clientRequestException(HttpStatusCode.NotFound))
		)
		assertEquals(
			MutationFailureKind.Terminal,
			spec.classifyError(mutation, clientRequestException(HttpStatusCode.BadRequest))
		)
	}

	@Test
	fun resolveFailure_deleteSyntheticTermNotFound_dropsEvenWhenRefreshFails() = runTest {
		val spec = specUnderTest(refreshRemoteSnapshot = {
			throw serverResponseException(HttpStatusCode.ServiceUnavailable)
		})

		val resolution = spec.resolveFailure(
			mutation = deleteSyntheticTermEnvelope(),
			throwable = clientRequestException(HttpStatusCode.NotFound)
		)

		val drop = assertIs<MutationFailureResolution.Drop<String, AcademicRecordMutation>>(resolution)
		assertTrue(drop.propagate)
	}

	@Test
	fun resolveFailure_deleteSyntheticTermNotFound_stillRefreshesSnapshotWhenAvailable() = runTest {
		var refreshCalls = 0
		val spec = specUnderTest(refreshRemoteSnapshot = {
			refreshCalls++
			versionedRecord(revision = 3L)
		})

		val resolution = spec.resolveFailure(
			mutation = deleteSyntheticTermEnvelope(),
			throwable = clientRequestException(HttpStatusCode.NotFound)
		)

		assertIs<MutationFailureResolution.Drop<String, AcademicRecordMutation>>(resolution)
		assertEquals(1, refreshCalls)
	}

	@Test
	fun resolveFailure_deleteAttemptOverrideNotFound_dropsEvenWhenRefreshFails() = runTest {
		val spec = specUnderTest(refreshRemoteSnapshot = {
			throw serverResponseException(HttpStatusCode.ServiceUnavailable)
		})

		val resolution = spec.resolveFailure(
			mutation = recordMutationEnvelope(AcademicRecordMutation.DeleteAttemptOverride("attempt-1")),
			throwable = clientRequestException(HttpStatusCode.NotFound)
		)

		assertIs<MutationFailureResolution.Drop<String, AcademicRecordMutation>>(resolution)
	}

	@Test
	fun resolveFailure_upsertOverrideConflict_whenRefreshFails_defersInsteadOfFailingTerminally() = runTest {
		val spec = specUnderTest(refreshRemoteSnapshot = {
			throw serverResponseException(HttpStatusCode.ServiceUnavailable)
		})

		val resolution = spec.resolveFailure(
			mutation = recordMutationEnvelope(AcademicRecordMutation.UpsertAttemptOverride("attempt-1")),
			throwable = clientRequestException(HttpStatusCode.Conflict)
		)

		assertIs<MutationFailureResolution.Defer<String, AcademicRecordMutation>>(resolution)
	}

	@Test
	fun resolveFailure_updateSyntheticTermConflict_whenRefreshSucceeds_retriesWithRefreshedRevision() = runTest {
		val spec = specUnderTest(refreshRemoteSnapshot = {
			versionedRecord(revision = 7L)
		})

		val resolution = spec.resolveFailure(
			mutation = recordMutationEnvelope(updateSyntheticTermMutation()),
			throwable = clientRequestException(HttpStatusCode.Conflict)
		)

		val retry = assertIs<MutationFailureResolution.Retry<String, AcademicRecordMutation>>(resolution)
		assertEquals(MutationPrecondition.Revision(7L), retry.mutation.precondition)
	}
}

private fun specUnderTest(
	refreshRemoteSnapshot: suspend () -> VersionedAcademicRecord = { error("not used") }
) = AcademicRecordMutationSyncSpec(
	remoteDataSource = errorRemoteDataSource(),
	persistConfirmedSnapshot = { },
	refreshRemoteSnapshot = refreshRemoteSnapshot
)

private fun deleteSyntheticTermEnvelope() = recordMutationEnvelope(
	AcademicRecordMutation.DeleteSyntheticTerm("2026-JUL_AUG")
)

private fun updateSyntheticTermMutation() = AcademicRecordMutation.UpdateSyntheticTerm(
	targetTermId = "2026-JUL_AUG",
	targetTermKey = "2026-JUL_AUG",
	termId = "2026-JUL_AUG",
	periodYear = 2026,
	periodCode = AcademicTermPeriod.JUL_AUG,
	attempts = emptyList()
)

private fun recordMutationEnvelope(
	command: AcademicRecordMutation
): MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
	mutationId = "mutation-1",
	scopeKey = RECORD_MUTATION_SCOPE,
	command = command,
	precondition = MutationPrecondition.Revision(1L),
	status = PendingMutationStatus.Pending,
	createdAt = 1L,
	updatedAt = 1L,
	lastError = null
)

private fun versionedRecord(revision: Long) = VersionedAcademicRecord(
	revision = revision,
	record = AcademicRecord(id = "record-1", terms = emptyList())
)

/**
 * resolveFailure never calls send, so every member errors.
 */
private fun errorRemoteDataSource(): AcademicRecordRemoteDataRepository =
	object : AcademicRecordRemoteDataRepository {
		override suspend fun getAcademicRecord(): VersionedAcademicRecord = error("not used")

		override suspend fun upsertAttemptOverride(
			attemptId: String,
			score: AttemptScore?,
			outcome: AttemptOutcome?,
			mutationId: String,
			expectedRevision: Long
		): VersionedAcademicRecord = error("not used")

		override suspend fun deleteAttemptOverride(
			attemptId: String,
			mutationId: String,
			expectedRevision: Long
		): VersionedAcademicRecord = error("not used")

		override suspend fun addSyntheticTerm(
			command: AcademicRecordMutation.AddSyntheticTerm,
			mutationId: String,
			expectedRevision: Long
		): VersionedAcademicRecord = error("not used")

		override suspend fun updateSyntheticTerm(
			command: AcademicRecordMutation.UpdateSyntheticTerm,
			mutationId: String,
			expectedRevision: Long
		): VersionedAcademicRecord = error("not used")

		override suspend fun deleteSyntheticTerm(
			termId: String,
			mutationId: String,
			expectedRevision: Long
		): VersionedAcademicRecord = error("not used")
	}
