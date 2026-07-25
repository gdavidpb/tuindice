package com.gdavidpb.tuindice.record.data.mutation

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.record.data.source.AcademicRecordDataSource
import com.gdavidpb.tuindice.record.data.source.FakeAcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.source.FakeIdentifierRepository
import com.gdavidpb.tuindice.record.data.source.FakeRecordSettingsDataRepository
import com.gdavidpb.tuindice.record.data.source.InMemoryMutationEnvelopeStore
import com.gdavidpb.tuindice.record.data.source.LaggyAcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse

/**
 * Regression coverage for the D-4 migration's confirm/retire ordering. Found by comparing
 * a live device run against `production`: creating a synthetic term left the newly
 * created term unselected, because a reactive observer could see a moment where the
 * mutation was neither in the outbox overlay NOR in the confirmed local record.
 *
 * `deletePendingBeforeConfirm` is the switch that decides which of the two writes lands
 * first. With it `true` (the bug), the outbox row disappears before the confirmed
 * snapshot is saved, opening exactly that window.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AcademicRecordMutationSyncSpecOrderingTest {
	@Test
	fun deletePendingBeforeConfirm_isFalse() {
		val spec = AcademicRecordMutationSyncSpec(
			remoteDataSource = errorRemoteDataSource(),
			persistConfirmedSnapshot = { },
			refreshRemoteSnapshot = { error("not used") }
		)

		val mutation: MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
			mutationId = "mutation-1",
			scopeKey = RECORD_MUTATION_SCOPE,
			command = AcademicRecordMutation.DeleteAttemptOverride("attempt-1"),
			precondition = MutationPrecondition.Revision(1L),
			status = PendingMutationStatus.Pending,
			createdAt = 1L,
			updatedAt = 1L,
			lastError = null
		)

		// The confirmed write must land before the outbox row disappears — never the
		// other way, or a reactive observer can catch the record with neither.
		assertFalse(spec.deletePendingBeforeConfirm(mutation))
	}

	@Test
	fun addSyntheticTerm_visibleRecordNeverLosesTheTermOnceItAppears() = runTest {
		val observedTermPresence = observeTermPresenceDuringAddSyntheticTerm(
			localDataSource = LaggyAcademicRecordLocalDataRepository(
				delegate = FakeAcademicRecordLocalDataRepository(
					record = VersionedAcademicRecord(
						revision = 1L,
						record = AcademicRecord(id = "record-1", terms = emptyList())
					)
				)
			)
		)

		assertTermVisibilityNeverRegressed(observedTermPresence)
	}
}

private fun assertTermVisibilityNeverRegressed(observedTermPresence: List<Boolean>) {
	val firstAppearance = observedTermPresence.indexOfFirst { present -> present }
	check(firstAppearance >= 0) { "The term never appeared in the visible flow at all." }

	val regressed = observedTermPresence.drop(firstAppearance + 1).any { present -> !present }
	assertFalse(regressed, "Term visibility regressed after first appearing: $observedTermPresence")
}

private suspend fun TestScope.observeTermPresenceDuringAddSyntheticTerm(
	localDataSource: AcademicRecordLocalDataRepository
): List<Boolean> {
	val recordWithTerm = academicRecordWithSyntheticTerm(TEST_TERM_ID)
	val addSyntheticTermStarted = CompletableDeferred<Unit>()
	val releaseAddSyntheticTerm = CompletableDeferred<Unit>()
	val dataSource = academicRecordDataSource(
		localDataSource = localDataSource,
		remoteDataSource = errorRemoteDataSource(
			addSyntheticTerm = {
				addSyntheticTermStarted.complete(Unit)
				releaseAddSyntheticTerm.await()
				VersionedAcademicRecord(revision = 2L, record = recordWithTerm)
			}
		),
		coroutineScope = this
	)

	val observedTermPresence = mutableListOf<Boolean>()
	val collector = launch {
		dataSource.observeAcademicRecordFlow().collect { record ->
			observedTermPresence += record.terms.any { term -> term.id == TEST_TERM_ID }
		}
	}
	runCurrent()

	val creation = launch { dataSource.addSyntheticTerm(syntheticTermCreationCommand(TEST_TERM_ID)) }
	addSyntheticTermStarted.await()
	runCurrent()

	releaseAddSyntheticTerm.complete(Unit)
	creation.join()
	advanceUntilIdle()
	collector.cancel()

	return observedTermPresence
}

private const val TEST_TERM_ID = "2026-SEP_DEC"

private fun academicRecordWithSyntheticTerm(termId: String) = AcademicRecord(
	id = "record-1",
	terms = listOf(
		AcademicTerm(
			id = termId,
			periodYear = 2026,
			periodCode = AcademicTermPeriod.SEP_DEC,
			kind = TermKind.SYNTHETIC,
			attempts = listOf(
				AcademicAttempt(
					id = "$termId-EC5333",
					subjectCode = "EC5333",
					subjectName = "Economia",
					credits = 4,
					gradingMode = AttemptGradingMode.NUMERIC
				)
			)
		)
	)
)

private fun syntheticTermCreationCommand(termId: String) = SyntheticTermCreationCommand(
	termId = termId,
	periodYear = 2026,
	periodCode = AcademicTermPeriod.SEP_DEC,
	attempts = listOf(
		SyntheticTermCreationCommand.SyntheticAttemptSeed(
			attemptId = "$termId-EC5333",
			subjectCode = "EC5333",
			subjectName = "Economia",
			credits = 4,
			gradingMode = AttemptGradingMode.NUMERIC,
			score = AttemptScore.empty(),
			outcome = AttemptOutcome.PENDING
		)
	)
)

private fun academicRecordDataSource(
	localDataSource: AcademicRecordLocalDataRepository,
	remoteDataSource: AcademicRecordRemoteDataRepository,
	coroutineScope: kotlinx.coroutines.CoroutineScope
): AcademicRecordDataSource {
	val outboxStore: MutationEnvelopeStore<String, AcademicRecordMutation> = InMemoryMutationEnvelopeStore()
	val mutationEngine = StoreBackedMutationEngine<String, AcademicRecordMutation, VersionedAcademicRecord>(
		storeId = "record-test",
		outboxStore = outboxStore,
		coroutineScope = coroutineScope
	)

	return AcademicRecordDataSource(
		localDataSource = localDataSource,
		remoteDataSource = remoteDataSource,
		settingsDataSource = FakeRecordSettingsDataRepository(),
		mutationEngine = mutationEngine,
		identifierRepository = FakeIdentifierRepository()
	)
}

/**
 * Every member errors unless overridden, so each test only supplies the one call it needs.
 */
private fun errorRemoteDataSource(
	addSyntheticTerm: suspend (AcademicRecordMutation.AddSyntheticTerm) -> VersionedAcademicRecord = { error("not used") }
): AcademicRecordRemoteDataRepository = object : AcademicRecordRemoteDataRepository {
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
	): VersionedAcademicRecord = addSyntheticTerm(command)

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
