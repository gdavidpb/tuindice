package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// A mutation id the caller names is the outbox row's id, the id the request carries, and the id its outcome is
// published under. Without one the data source generates it. A write it cannot submit is rejected under that name.
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AcademicRecordDataSourceMutationIdTest {
	@Test
	fun upsertAttemptOverride_withAnExternalMutationId_usesItForTheEnvelopeAndTheRequest() = runTest {
		val outbox = InMemoryMutationEnvelopeStore()
		val remote = OutboxObservingRemoteDataRepository(remoteAnswering(), outbox)
		val dataSource = dataSource(store = outbox, remote = remote)

		dataSource.upsertAttemptOverride(
			attemptId = "attempt-1",
			score = AttemptScore.numeric(5),
			outcome = null,
			mutationId = "external-upsert"
		)

		assertEquals(listOf("external-upsert"), remote.requestedMutationIds)
		assertEquals(listOf(listOf("external-upsert")), remote.outboxIdsAtRequest)
	}

	@Test
	fun upsertAttemptOverride_withoutAnExternalMutationId_generatesOne() = runTest {
		val outbox = InMemoryMutationEnvelopeStore()
		val remote = OutboxObservingRemoteDataRepository(remoteAnswering(), outbox)
		val dataSource = dataSource(store = outbox, remote = remote)

		dataSource.upsertAttemptOverride(
			attemptId = "attempt-1",
			score = AttemptScore.numeric(5),
			outcome = null,
			mutationId = null
		)

		assertEquals(listOf("mutation-1"), remote.requestedMutationIds)
		assertEquals(listOf(listOf("mutation-1")), remote.outboxIdsAtRequest)
	}

	@Test
	fun addSyntheticTerm_withAnExternalMutationId_usesItForTheEnvelopeAndTheRequest() = runTest {
		val outbox = InMemoryMutationEnvelopeStore()
		val remote = OutboxObservingRemoteDataRepository(remoteAnswering(), outbox)
		val dataSource = dataSource(store = outbox, remote = remote)

		dataSource.addSyntheticTerm(syntheticTermCreation(mutationId = "external-add"))

		assertEquals(listOf("external-add"), remote.requestedMutationIds)
		assertEquals(listOf(listOf("external-add")), remote.outboxIdsAtRequest)
	}

	@Test
	fun updateSyntheticTerm_withAnExternalMutationId_usesItForTheEnvelopeAndTheRequest() = runTest {
		val outbox = InMemoryMutationEnvelopeStore()
		val remote = OutboxObservingRemoteDataRepository(remoteAnswering(), outbox)
		val dataSource = dataSource(store = outbox, remote = remote)

		dataSource.updateSyntheticTerm(syntheticTermUpdate(mutationId = "external-update"))

		assertEquals(listOf("external-update"), remote.requestedMutationIds)
		assertEquals(listOf(listOf("external-update")), remote.outboxIdsAtRequest)
	}

	@Test
	fun deleteSyntheticTerm_withAnExternalMutationId_usesItForTheEnvelopeAndTheRequest() = runTest {
		val outbox = InMemoryMutationEnvelopeStore()
		val remote = OutboxObservingRemoteDataRepository(remoteAnswering(), outbox)
		val dataSource = dataSource(store = outbox, remote = remote)

		dataSource.deleteSyntheticTerm(termId = "syn-1", mutationId = "external-delete")

		assertEquals(listOf("external-delete"), remote.requestedMutationIds)
		assertEquals(listOf(listOf("external-delete")), remote.outboxIdsAtRequest)
	}

	@Test
	fun writesWithoutLocalRecord_areRejectedWithNoRecordUnderTheirExternalIds() = runTest {
		val dataSource = dataSource(local = localWith(record = null))
		val outcomes = collectOutcomes(dataSource)

		dataSource.upsertAttemptOverride(
			attemptId = "attempt-1",
			score = AttemptScore.numeric(5),
			outcome = null,
			mutationId = "upsert"
		)
		dataSource.deleteAttemptOverride(attemptId = "attempt-1", mutationId = "delete-attempt")
		dataSource.addSyntheticTerm(syntheticTermCreation(mutationId = "add"))
		dataSource.updateSyntheticTerm(syntheticTermUpdate(mutationId = "update"))
		dataSource.deleteSyntheticTerm(termId = "syn-1", mutationId = "delete-term")

		assertEquals(
			listOf(
				rejected("upsert", "no_record"),
				rejected("delete-attempt", "no_record"),
				rejected("add", "no_record"),
				rejected("update", "no_record"),
				rejected("delete-term", "no_record")
			),
			outcomes
		)
	}

	@Test
	fun writesWithoutRevision_areRejectedWithNoRevisionUnderTheirExternalIds() = runTest {
		val dataSource = dataSource(local = withoutRevision(syntheticRecord()))
		val outcomes = collectOutcomes(dataSource)

		dataSource.upsertAttemptOverride(
			attemptId = "attempt-1",
			score = AttemptScore.numeric(5),
			outcome = null,
			mutationId = "upsert"
		)
		dataSource.deleteAttemptOverride(attemptId = "attempt-1", mutationId = "delete-attempt")
		dataSource.addSyntheticTerm(syntheticTermCreation(mutationId = "add"))
		dataSource.updateSyntheticTerm(syntheticTermUpdate(mutationId = "update"))
		dataSource.deleteSyntheticTerm(termId = "syn-1", mutationId = "delete-term")

		assertEquals(
			listOf(
				rejected("upsert", "no_revision"),
				rejected("delete-attempt", "no_revision"),
				rejected("add", "no_revision"),
				rejected("update", "no_revision"),
				rejected("delete-term", "no_revision")
			),
			outcomes
		)
	}

	@Test
	fun deleteSyntheticTerm_withoutTheTerm_isRejectedWithNoTermUnderTheExternalId() = runTest {
		val dataSource = dataSource(local = localWith(syntheticRecord()))
		val outcomes = collectOutcomes(dataSource)

		dataSource.deleteSyntheticTerm(termId = "missing-term", mutationId = "delete-term")

		assertEquals(listOf(rejected("delete-term", "no_term")), outcomes)
	}

	@Test
	fun rejectedWrites_withoutAnExternalMutationId_publishNothing() = runTest {
		val noRecord = dataSource(local = localWith(record = null))
		val noRevision = dataSource(local = withoutRevision(syntheticRecord()))
		val noTerm = dataSource(local = localWith(syntheticRecord()))
		val noRecordOutcomes = collectOutcomes(noRecord)
		val noRevisionOutcomes = collectOutcomes(noRevision)
		val noTermOutcomes = collectOutcomes(noTerm)

		noRecord.upsertAttemptOverride(
			attemptId = "attempt-1",
			score = AttemptScore.numeric(5),
			outcome = null,
			mutationId = null
		)
		noRecord.addSyntheticTerm(syntheticTermCreation(mutationId = null))
		noRevision.deleteAttemptOverride(attemptId = "attempt-1", mutationId = null)
		noRevision.deleteSyntheticTerm(termId = "syn-1", mutationId = null)
		noTerm.deleteSyntheticTerm(termId = "missing-term", mutationId = null)

		runCurrent()

		assertTrue(noRecordOutcomes.isEmpty())
		assertTrue(noRevisionOutcomes.isEmpty())
		assertTrue(noTermOutcomes.isEmpty())
	}

	private fun TestScope.dataSource(
		local: AcademicRecordLocalDataRepository = localWith(syntheticRecord()),
		remote: AcademicRecordRemoteDataRepository = remoteAnswering(),
		store: InMemoryMutationEnvelopeStore = InMemoryMutationEnvelopeStore()
	) = AcademicRecordDataSource(
		localDataSource = local,
		remoteDataSource = remote,
		settingsDataSource = FakeRecordSettingsDataRepository(),
		mutationEngine = createMutationEngine(coroutineScope = backgroundScope, outboxStore = store),
		identifierRepository = FakeIdentifierRepository()
	)

	// Collects what the data source publishes from the moment the test starts, as the outcomes are settled.
	private fun TestScope.collectOutcomes(dataSource: AcademicRecordDataSource): MutableList<MutationOutcome> {
		val collected = mutableListOf<MutationOutcome>()

		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
			dataSource.observeMutationOutcomesFlow().collect { outcome -> collected += outcome }
		}

		return collected
	}

	private fun remoteAnswering() = ControlledAcademicRecordRemoteDataRepository(
		upsertResponse = defaultVersionedRecord(revision = 2L)
	)

	private fun localWith(record: VersionedAcademicRecord?): AcademicRecordLocalDataRepository {
		return FakeAcademicRecordLocalDataRepository(record = record)
	}

	private fun withoutRevision(record: VersionedAcademicRecord): AcademicRecordLocalDataRepository {
		return WithoutRevisionLocalDataRepository(FakeAcademicRecordLocalDataRepository(record = record))
	}

	private fun syntheticRecord(): VersionedAcademicRecord = VersionedAcademicRecord(
		revision = 1L,
		record = AcademicRecord(
			id = "record-1",
			terms = listOf(
				AcademicTerm(
					id = "syn-1",
					periodYear = 9999,
					periodCode = AcademicTermPeriod.JAN_MAR,
					kind = TermKind.SYNTHETIC,
					attempts = emptyList()
				)
			)
		)
	)

	private fun syntheticTermCreation(mutationId: String?) = SyntheticTermCreationCommand(
		termId = "syn-2",
		periodYear = 9999,
		periodCode = AcademicTermPeriod.APR_JUL,
		attempts = emptyList(),
		mutationId = mutationId
	)

	private fun syntheticTermUpdate(mutationId: String?) = SyntheticTermUpdateCommand(
		targetTermId = "syn-1",
		targetTermKey = "9999-JAN_MAR",
		termId = "syn-1",
		periodYear = 9999,
		periodCode = AcademicTermPeriod.JAN_MAR,
		attempts = emptyList(),
		mutationId = mutationId
	)

	private fun rejected(mutationId: String, detailCode: String) = MutationOutcome(
		mutationId = mutationId,
		kind = MutationOutcomeKind.REJECTED,
		detailCode = detailCode
	)
}

// Records each request's mutation id, and the outbox rows present when the request leaves: the row the request is
// built from is observed in the outbox at the moment it is sent, not only inside the request.
private class OutboxObservingRemoteDataRepository(
	private val delegate: AcademicRecordRemoteDataRepository,
	private val outbox: InMemoryMutationEnvelopeStore
) : AcademicRecordRemoteDataRepository by delegate {
	val requestedMutationIds = mutableListOf<String>()
	val outboxIdsAtRequest = mutableListOf<List<String>>()

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		recordRequest(mutationId)
		return delegate.upsertAttemptOverride(attemptId, score, outcome, mutationId, expectedRevision)
	}

	override suspend fun addSyntheticTerm(
		command: AcademicRecordMutation.AddSyntheticTerm,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		recordRequest(mutationId)
		return delegate.addSyntheticTerm(command, mutationId, expectedRevision)
	}

	override suspend fun updateSyntheticTerm(
		command: AcademicRecordMutation.UpdateSyntheticTerm,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		recordRequest(mutationId)
		return delegate.updateSyntheticTerm(command, mutationId, expectedRevision)
	}

	override suspend fun deleteSyntheticTerm(
		termRef: String,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		recordRequest(mutationId)
		return delegate.deleteSyntheticTerm(termRef, mutationId, expectedRevision)
	}

	private suspend fun recordRequest(mutationId: String) {
		requestedMutationIds += mutationId
		outboxIdsAtRequest += outbox.getMutations(RECORD_MUTATION_SCOPE).map { mutation -> mutation.mutationId }
	}
}

// A local store whose record is there while its revision is not: the state the data source rejects.
private class WithoutRevisionLocalDataRepository(
	private val delegate: AcademicRecordLocalDataRepository
) : AcademicRecordLocalDataRepository by delegate {
	override suspend fun getRecordRevision(): Long? = null
}
