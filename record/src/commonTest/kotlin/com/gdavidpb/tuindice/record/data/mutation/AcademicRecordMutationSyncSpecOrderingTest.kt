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
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.record.data.source.AcademicRecordDataSource
import com.gdavidpb.tuindice.record.data.source.FakeAcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.source.FakeIdentifierRepository
import com.gdavidpb.tuindice.record.data.source.FakeRecordSettingsDataRepository
import com.gdavidpb.tuindice.record.data.source.InMemoryMutationEnvelopeStore
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
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
		val recordWithTerm = academicRecordWithSyntheticTerm(TEST_TERM_ID)
		val dataSource = academicRecordDataSource(
			initialRecord = AcademicRecord(id = "record-1", terms = emptyList()),
			remoteDataSource = errorRemoteDataSource(
				addSyntheticTerm = { VersionedAcademicRecord(revision = 2L, record = recordWithTerm) }
			),
			coroutineScope = this
		)

		val observedTermPresence = mutableListOf<Boolean>()
		val collector = launch {
			dataSource.observeAcademicRecordFlow().collect { record ->
				observedTermPresence += record.terms.any { term -> term.id == TEST_TERM_ID }
			}
		}

		dataSource.addSyntheticTerm(syntheticTermCreationCommand(TEST_TERM_ID))
		advanceUntilIdle()
		collector.cancel()

		val firstAppearance = observedTermPresence.indexOfFirst { present -> present }
		check(firstAppearance >= 0) { "The term never appeared in the visible flow at all." }

		// Once the term is visible, it must never disappear again — that regression is
		// exactly what let a reactive selector observe "term not visible" mid-confirm
		// and clobber an explicit selection back to the previous term.
		val regressed = observedTermPresence.drop(firstAppearance + 1).any { present -> !present }
		assertFalse(regressed, "Term visibility regressed after first appearing: $observedTermPresence")
	}
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
	initialRecord: AcademicRecord,
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
		localDataSource = FakeAcademicRecordLocalDataRepository(
			record = VersionedAcademicRecord(revision = 1L, record = initialRecord)
		),
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
