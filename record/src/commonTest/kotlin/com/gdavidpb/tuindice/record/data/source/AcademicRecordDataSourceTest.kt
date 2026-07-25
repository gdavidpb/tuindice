package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptGradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.persistence.domain.mutation.DEFAULT_FAILED_RETRY_BACKOFF_MILLIS
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelopeStore
import com.gdavidpb.tuindice.persistence.domain.mutation.StoreBackedMutationEngine
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.record.data.model.VersionedAcademicRecord
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordLocalDataRepository
import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.record.data.repository.RecordSettingsDataRepository
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AcademicRecordDataSourceTest {
	@Test
	fun updateAcademicRecord_ignoresCooldown_whenLocalRecordIsMissing() = runTest {
		val remoteRecord = defaultVersionedRecord()
		val localDataSource = FakeAcademicRecordLocalDataRepository(
			record = null,
			hasSyncedRecord = true
		)
		val remoteDataSource = ControlledAcademicRecordRemoteDataRepository(upsertResponse = remoteRecord)
		val settingsDataSource = FakeRecordSettingsDataRepository(onCooldown = true)
		val dataSource = AcademicRecordDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource,
			mutationEngine = createMutationEngine(this),
			identifierRepository = FakeIdentifierRepository()
		)

		dataSource.updateAcademicRecord()

		assertEquals(1, remoteDataSource.getRecordCalls)
		assertEquals(listOf(remoteRecord), localDataSource.savedRecords)
		assertEquals(true, settingsDataSource.cooldownMarked)
	}

	@Test
	fun updateAcademicRecord_ignoresCooldown_whenRecordHasNeverSynced() = runTest {
		val remoteRecord = defaultVersionedRecord(revision = 2L)
		val localDataSource = FakeAcademicRecordLocalDataRepository(
			record = defaultVersionedRecord(),
			hasSyncedRecord = false
		)
		val remoteDataSource = ControlledAcademicRecordRemoteDataRepository(upsertResponse = remoteRecord)
		val settingsDataSource = FakeRecordSettingsDataRepository(onCooldown = true)
		val dataSource = AcademicRecordDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource,
			mutationEngine = createMutationEngine(this),
			identifierRepository = FakeIdentifierRepository()
		)

		dataSource.updateAcademicRecord()

		assertEquals(1, remoteDataSource.getRecordCalls)
		assertEquals(listOf(remoteRecord), localDataSource.savedRecords)
		assertEquals(true, settingsDataSource.cooldownMarked)
	}

	@Test
	fun updateAcademicRecord_respectsCooldown_whenLocalRecordIsUsable() = runTest {
		val localDataSource = FakeAcademicRecordLocalDataRepository(
			record = defaultVersionedRecord(),
			hasSyncedRecord = true
		)
		val remoteDataSource = ControlledAcademicRecordRemoteDataRepository(
			upsertResponse = defaultVersionedRecord(revision = 2L)
		)
		val settingsDataSource = FakeRecordSettingsDataRepository(onCooldown = true)
		val dataSource = AcademicRecordDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource,
			mutationEngine = createMutationEngine(this),
			identifierRepository = FakeIdentifierRepository()
		)

		dataSource.updateAcademicRecord()

		assertEquals(0, remoteDataSource.getRecordCalls)
		assertEquals(emptyList(), localDataSource.savedRecords)
		assertEquals(false, settingsDataSource.cooldownMarked)
	}

	@Test
	fun updateAcademicRecord_forceRemote_ignoresCooldown_whenLocalRecordIsUsable() = runTest {
		val remoteRecord = defaultVersionedRecord(revision = 2L)
		val localDataSource = FakeAcademicRecordLocalDataRepository(
			record = defaultVersionedRecord(),
			hasSyncedRecord = true
		)
		val remoteDataSource = ControlledAcademicRecordRemoteDataRepository(upsertResponse = remoteRecord)
		val settingsDataSource = FakeRecordSettingsDataRepository(onCooldown = true)
		val dataSource = AcademicRecordDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource,
			mutationEngine = createMutationEngine(this),
			identifierRepository = FakeIdentifierRepository()
		)

		dataSource.updateAcademicRecord(forceRemote = true)

		assertEquals(1, remoteDataSource.getRecordCalls)
		assertEquals(listOf(remoteRecord), localDataSource.savedRecords)
		assertEquals(true, settingsDataSource.cooldownMarked)
	}

	@Test
	fun upsertAttemptOverride_whenEarlierAckArrivesAfterNewerTap_keepsLaterPendingOverrideVisible() = runTest {
		val firstAttemptId = "11111111111111111111111111111111"
		val secondAttemptId = "22222222222222222222222222222222"
		val initialRecord = AcademicRecord(
			id = "record-1",
			terms = listOf(
				AcademicTerm(
					id = "term-1",
					periodYear = 2026,
					periodCode = AcademicTermPeriod.JAN_MAR,
					kind = TermKind.CURRENT,
					attempts = listOf(
						AcademicAttempt(
							id = firstAttemptId,
							subjectCode = "PB5611",
							subjectName = "Probabilidad",
							credits = 10,
							academicScore = AttemptScore.numeric(3),
							academicOutcome = AttemptOutcome.APPROVED
						),
						AcademicAttempt(
							id = secondAttemptId,
							subjectCode = "MA1001",
							subjectName = "Calculo",
							credits = 10,
							academicScore = AttemptScore.numeric(3),
							academicOutcome = AttemptOutcome.APPROVED
						)
					)
				)
			),
			attemptOverrides = emptyList()
		)
		val firstAckRecord = initialRecord.copy(
			attemptOverrides = listOf(
				AttemptOverride(
					attemptId = firstAttemptId,
					score = AttemptScore.numeric(4),
					updatedAtMillis = 2L
				)
			)
		)
		val secondAckRecord = firstAckRecord.copy(
			attemptOverrides = firstAckRecord.attemptOverrides + AttemptOverride(
				attemptId = secondAttemptId,
				score = AttemptScore.numeric(5),
				updatedAtMillis = 3L
			)
		)
		val localDataSource = FakeAcademicRecordLocalDataRepository(
			record = VersionedAcademicRecord(
				revision = 1L,
				record = initialRecord
			)
		)
		val remoteDataSource = DelayedSequentialUpsertAcademicRecordRemoteDataRepository(
			firstResponse = VersionedAcademicRecord(
				revision = 2L,
				record = firstAckRecord
			),
			secondResponse = VersionedAcademicRecord(
				revision = 3L,
				record = secondAckRecord
			)
		)
		val dataSource = AcademicRecordDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeRecordSettingsDataRepository(),
			mutationEngine = createMutationEngine(this),
			identifierRepository = FakeIdentifierRepository()
		)

		val visibleGrades = mutableListOf<Int?>()
		val collectorJob = launch {
			dataSource.observeAcademicRecordFlow().collect { record ->
				visibleGrades += record.attemptOverrides
					.firstOrNull { override -> override.attemptId == secondAttemptId }
					?.score
					?.numericValue
			}
		}

		val firstUpsertJob = async {
			dataSource.upsertAttemptOverride(
				attemptId = firstAttemptId,
				score = AttemptScore.numeric(4),
				outcome = null
			)
		}
		remoteDataSource.firstUpsertStarted.await()

		val secondUpsertJob = async {
			dataSource.upsertAttemptOverride(
				attemptId = secondAttemptId,
				score = AttemptScore.numeric(5),
				outcome = null
			)
		}

		remoteDataSource.releaseFirstUpsert.complete(Unit)

		firstUpsertJob.await()
		secondUpsertJob.await()

		advanceUntilIdle()
		collectorJob.cancel()

		// El ack anterior no puede retroceder el override mas nuevo: el estado visible
		// se deriva del outbox, donde solo queda la ultima mutacion por replaceKey.
		assertEquals(listOf(5), visibleGrades.filterNotNull().distinct())
		assertEquals(
			listOf(
				AttemptOverride(
					attemptId = firstAttemptId,
					score = AttemptScore.numeric(4),
					updatedAtMillis = 2L
				),
				AttemptOverride(
					attemptId = secondAttemptId,
					score = AttemptScore.numeric(5),
					updatedAtMillis = 3L
				)
			),
			requireNotNull(dataSource.getAcademicRecord()).attemptOverrides
		)
	}

	@Test
	fun upsertAttemptOverride_whenSupersededRevisionFails_rebasesWithoutRestoringOldGrade() = runTest {
		val attemptId = "33333333333333333333333333333333"
		val initialRecord = AcademicRecord(
			id = "record-1",
			terms = listOf(
				AcademicTerm(
					id = "term-1",
					periodYear = 2026,
					periodCode = AcademicTermPeriod.JAN_MAR,
					kind = TermKind.CURRENT,
					attempts = listOf(
						AcademicAttempt(
							id = attemptId,
							subjectCode = "PB5611",
							subjectName = "Probabilidad",
							credits = 10,
							academicScore = AttemptScore.numeric(3),
							academicOutcome = AttemptOutcome.APPROVED
						)
					)
				)
			),
			attemptOverrides = emptyList()
		)
		val staleRemoteRecord = initialRecord.copy(
			attemptOverrides = listOf(
				AttemptOverride(
					attemptId = attemptId,
					score = AttemptScore.numeric(4),
					updatedAtMillis = 2L
				)
			)
		)
		val latestRemoteRecord = initialRecord.copy(
			attemptOverrides = listOf(
				AttemptOverride(
					attemptId = attemptId,
					score = AttemptScore.numeric(5),
					updatedAtMillis = 3L
				)
			)
		)
		val localDataSource = FakeAcademicRecordLocalDataRepository(
			record = VersionedAcademicRecord(
				revision = 2L,
				record = initialRecord
			)
		)
		val remoteDataSource = RebasingAcademicRecordRemoteDataRepository(
			staleResponse = VersionedAcademicRecord(
				revision = 3L,
				record = staleRemoteRecord
			),
			latestResponse = VersionedAcademicRecord(
				revision = 4L,
				record = latestRemoteRecord
			)
		)
		val dataSource = AcademicRecordDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeRecordSettingsDataRepository(),
			mutationEngine = createMutationEngine(this),
			identifierRepository = FakeIdentifierRepository()
		)

		val visibleGrades = mutableListOf<Int?>()
		val collectorJob = launch {
			dataSource.observeAcademicRecordFlow().collect { record ->
				visibleGrades += record.attemptOverrides
					.firstOrNull { override -> override.attemptId == attemptId }
					?.score
					?.numericValue
			}
		}

		val firstUpsertJob = async {
			dataSource.upsertAttemptOverride(
				attemptId = attemptId,
				score = AttemptScore.numeric(4),
				outcome = null
			)
		}
		remoteDataSource.firstUpsertStarted.await()

		val secondUpsertJob = async {
			dataSource.upsertAttemptOverride(
				attemptId = attemptId,
				score = AttemptScore.numeric(5),
				outcome = null
			)
		}

		remoteDataSource.releaseFirstUpsert.complete(Unit)

		firstUpsertJob.await()
		secondUpsertJob.await()

		assertEquals(listOf("mutation-1", "mutation-2", "mutation-2"), remoteDataSource.upsertAttemptMutationIds)
		assertEquals(listOf(2L, 2L, 3L), remoteDataSource.upsertAttemptExpectedRevisions)
		advanceUntilIdle()
		collectorJob.cancel()

		assertEquals(listOf(4, 5), visibleGrades.filterNotNull().distinct())
		assertEquals(
			listOf(
				AttemptOverride(
					attemptId = attemptId,
					score = AttemptScore.numeric(5),
					updatedAtMillis = 3L
				)
			),
			requireNotNull(dataSource.getAcademicRecord()).attemptOverrides
		)
	}

	@Test
	fun deleteAttemptOverride_whenSupersededByLaterUpsert_suppressesStaleFailure_and_keepsLatestOverride() = runTest {
		val attemptId = "44444444444444444444444444444444"
		val initialRecord = AcademicRecord(
			id = "record-1",
			terms = listOf(
				AcademicTerm(
					id = "term-1",
					periodYear = 2026,
					periodCode = AcademicTermPeriod.JAN_MAR,
					kind = TermKind.CURRENT,
					attempts = listOf(
						AcademicAttempt(
							id = attemptId,
							subjectCode = "PB5611",
							subjectName = "Probabilidad",
							credits = 10,
							academicScore = AttemptScore.numeric(3),
							academicOutcome = AttemptOutcome.FAILED
						)
					)
				)
			),
			attemptOverrides = listOf(
				AttemptOverride(
					attemptId = attemptId,
					score = AttemptScore.numeric(4),
					updatedAtMillis = 1L
				)
			)
		)
		val remoteRecord = initialRecord.copy(
			attemptOverrides = listOf(
				AttemptOverride(
					attemptId = attemptId,
					score = AttemptScore.numeric(5),
					updatedAtMillis = 2L
				)
			)
		)
		val localDataSource = FakeAcademicRecordLocalDataRepository(
			record = VersionedAcademicRecord(
				revision = 2L,
				record = initialRecord
			)
		)
		val remoteDataSource = ControlledAcademicRecordRemoteDataRepository(
			upsertResponse = VersionedAcademicRecord(
				revision = 3L,
				record = remoteRecord
			)
		)
		val dataSource = AcademicRecordDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeRecordSettingsDataRepository(),
			mutationEngine = createMutationEngine(this),
			identifierRepository = FakeIdentifierRepository()
		)

		val deleteJob = async {
			dataSource.deleteAttemptOverride(attemptId)
		}
		remoteDataSource.deleteStarted.await()

		val upsertJob = async {
			dataSource.upsertAttemptOverride(
				attemptId = attemptId,
				score = AttemptScore.numeric(5),
				outcome = null
			)
		}

		remoteDataSource.releaseDeleteFailure.complete(Unit)

		deleteJob.await()
		upsertJob.await()

		assertEquals(1, remoteDataSource.deleteAttemptCalls)
		assertEquals(1, remoteDataSource.upsertAttemptCalls)
		assertEquals(0, remoteDataSource.getRecordCalls)
		assertEquals(listOf("mutation-1"), remoteDataSource.deleteAttemptMutationIds)
		assertEquals(listOf(2L), remoteDataSource.deleteAttemptExpectedRevisions)
		assertEquals(listOf("mutation-2"), remoteDataSource.upsertAttemptMutationIds)
		assertEquals(listOf(2L), remoteDataSource.upsertAttemptExpectedRevisions)
		assertEquals(
			listOf(
				AttemptOverride(
					attemptId = attemptId,
					score = AttemptScore.numeric(5),
					updatedAtMillis = 2L
				)
			),
			requireNotNull(dataSource.getAcademicRecord()).attemptOverrides
		)
	}

	@Test
	fun updateSyntheticTerm_appliesLocalChange_andSubmitsPatchMutation() = runTest {
		val retainedAttemptId = "retained-attempt"
		val removedAttemptId = "removed-attempt"
		val initialRecord = AcademicRecord(
			id = "record-1",
			terms = listOf(
				AcademicTerm(
					id = "2026-JUL_AUG",
					periodYear = 2026,
					periodCode = AcademicTermPeriod.JUL_AUG,
					kind = TermKind.SYNTHETIC,
					attempts = listOf(
						AcademicAttempt(
							id = retainedAttemptId,
							subjectCode = "MA1111",
							subjectName = "Matematicas I",
							credits = 4,
							gradingMode = AttemptGradingMode.NUMERIC
						),
						AcademicAttempt(
							id = removedAttemptId,
							subjectCode = "FS1111",
							subjectName = "Fisica I",
							credits = 4,
							gradingMode = AttemptGradingMode.NUMERIC
						)
					)
				)
			),
			attemptOverrides = listOf(
				AttemptOverride(
					attemptId = removedAttemptId,
					score = AttemptScore.numeric(5),
					updatedAtMillis = 1L
				)
			)
		)
		val updatedRecord = initialRecord.copy(
			terms = listOf(
				AcademicTerm(
					id = "2026-JUL_AUG",
					periodYear = 2026,
					periodCode = AcademicTermPeriod.JUL_AUG,
					kind = TermKind.SYNTHETIC,
					attempts = listOf(
						AcademicAttempt(
							id = retainedAttemptId,
							subjectCode = "MA1111",
							subjectName = "Matematicas I",
							credits = 4,
							gradingMode = AttemptGradingMode.NUMERIC
						)
					)
				)
			),
			attemptOverrides = emptyList()
		)
		val localDataSource = FakeAcademicRecordLocalDataRepository(
			record = VersionedAcademicRecord(
				revision = 12L,
				record = initialRecord
			)
		)
		val remoteDataSource = ControlledAcademicRecordRemoteDataRepository(
			upsertResponse = VersionedAcademicRecord(
				revision = 13L,
				record = updatedRecord
			)
		)
		val dataSource = AcademicRecordDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeRecordSettingsDataRepository(),
			mutationEngine = createMutationEngine(this),
			identifierRepository = FakeIdentifierRepository()
		)

		dataSource.updateSyntheticTerm(
			SyntheticTermUpdateCommand(
				targetTermId = "2026-JUL_AUG",
				targetTermKey = "2026-JUL_AUG",
				termId = "2026-JUL_AUG",
				periodYear = 2026,
				periodCode = AcademicTermPeriod.JUL_AUG,
				attempts = listOf(
					SyntheticTermUpdateCommand.SyntheticAttemptSeed(
						attemptId = retainedAttemptId,
						subjectCode = "MA1111",
						subjectName = "Matematicas I",
						credits = 4,
						gradingMode = AttemptGradingMode.NUMERIC,
						score = AttemptScore.empty(),
						outcome = AttemptOutcome.PENDING
					)
				)
			)
		)

		assertEquals(1, remoteDataSource.updateSyntheticTermCalls)
		assertEquals(listOf("mutation-1"), remoteDataSource.updateSyntheticTermMutationIds)
		assertEquals(listOf(12L), remoteDataSource.updateSyntheticTermExpectedRevisions)
		assertEquals(listOf("MA1111"), remoteDataSource.updateSyntheticTermCommands.single().attempts.map { attempt ->
			attempt.subjectCode
		})
		assertEquals(updatedRecord, requireNotNull(dataSource.getAcademicRecord()))
	}

	@Test
	fun deleteSyntheticTerm_appliesLocalChange_andSubmitsDeleteMutation() = runTest {
		val removedAttemptId = "synthetic-attempt"
		val initialRecord = AcademicRecord(
			id = "record-1",
			terms = listOf(
				AcademicTerm(
					id = "2026-SEP_DEC",
					periodYear = 2026,
					periodCode = AcademicTermPeriod.SEP_DEC,
					kind = TermKind.SYNTHETIC,
					attempts = listOf(
						AcademicAttempt(
							id = removedAttemptId,
							subjectCode = "EC5201",
							subjectName = "Economia",
							credits = 4,
							gradingMode = AttemptGradingMode.NUMERIC
						)
					)
				)
			),
			attemptOverrides = listOf(
				AttemptOverride(
					attemptId = removedAttemptId,
					score = AttemptScore.numeric(5),
					updatedAtMillis = 1L
				)
			)
		)
		val updatedRecord = initialRecord.copy(
			terms = emptyList(),
			attemptOverrides = emptyList()
		)
		val localDataSource = FakeAcademicRecordLocalDataRepository(
			record = VersionedAcademicRecord(
				revision = 12L,
				record = initialRecord
			)
		)
		val remoteDataSource = ControlledAcademicRecordRemoteDataRepository(
			upsertResponse = VersionedAcademicRecord(
				revision = 13L,
				record = updatedRecord
			)
		)
		val dataSource = AcademicRecordDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeRecordSettingsDataRepository(),
			mutationEngine = createMutationEngine(this),
			identifierRepository = FakeIdentifierRepository()
		)

		dataSource.deleteSyntheticTerm("2026-SEP_DEC")

		assertEquals(1, remoteDataSource.deleteSyntheticTermCalls)
		assertEquals(listOf("2026-SEP_DEC"), remoteDataSource.deleteSyntheticTermIds)
		assertEquals(listOf("mutation-1"), remoteDataSource.deleteSyntheticTermMutationIds)
		assertEquals(listOf(12L), remoteDataSource.deleteSyntheticTermExpectedRevisions)
		assertEquals(updatedRecord, requireNotNull(dataSource.getAcademicRecord()))
	}

	@Test
	fun deleteSyntheticTerm_ignoresNonSyntheticOrMissingTermWithoutSubmittingMutation() = runTest {
		val initialRecord = AcademicRecord(
			id = "record-1",
			terms = listOf(
				AcademicTerm(
					id = "2026-JAN_MAR",
					periodYear = 2026,
					periodCode = AcademicTermPeriod.JAN_MAR,
					kind = TermKind.CURRENT
				)
			)
		)
		val localDataSource = FakeAcademicRecordLocalDataRepository(
			record = VersionedAcademicRecord(
				revision = 12L,
				record = initialRecord
			)
		)
		val remoteDataSource = ControlledAcademicRecordRemoteDataRepository(
			upsertResponse = VersionedAcademicRecord(
				revision = 13L,
				record = initialRecord
			)
		)
		val dataSource = AcademicRecordDataSource(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeRecordSettingsDataRepository(),
			mutationEngine = createMutationEngine(this),
			identifierRepository = FakeIdentifierRepository()
		)

		dataSource.deleteSyntheticTerm("2026-JAN_MAR")
		dataSource.deleteSyntheticTerm("2026-APR_JUL")

		assertEquals(0, remoteDataSource.deleteSyntheticTermCalls)
		assertEquals(initialRecord, requireNotNull(dataSource.getAcademicRecord()))
	}
}

internal const val DEFAULT_CONFIRMED_LANDING_LAG_MILLIS = 50L

internal fun defaultVersionedRecord(
	revision: Long = 1L
) = VersionedAcademicRecord(
	revision = revision,
	record = AcademicRecord(id = "record-1")
)

internal fun createMutationEngine(
	coroutineScope: CoroutineScope,
	outboxStore: MutationEnvelopeStore<String, AcademicRecordMutation> = InMemoryMutationEnvelopeStore(),
	failedRetryBackoffMillis: Long = DEFAULT_FAILED_RETRY_BACKOFF_MILLIS
) = StoreBackedMutationEngine<String, AcademicRecordMutation, VersionedAcademicRecord>(
	storeId = "record-test",
	outboxStore = outboxStore,
	coroutineScope = coroutineScope,
	failedRetryBackoffMillis = failedRetryBackoffMillis
)

internal class FakeAcademicRecordLocalDataRepository(
	record: VersionedAcademicRecord?,
	private val hasSyncedRecord: Boolean = true
) : AcademicRecordLocalDataRepository {
	private val recordState = MutableStateFlow(record?.record)
	private val stateHistory = mutableListOf<AcademicRecord>().apply {
		record?.record?.let(::add)
	}
	private var revision = record?.revision
	val savedRecords = mutableListOf<VersionedAcademicRecord>()

	override fun observeAcademicRecordFlow(): Flow<AcademicRecord?> = recordState

	override fun observeHasSyncedRecordFlow(): Flow<Boolean> = flowOf(hasSyncedRecord)

	override suspend fun hasAcademicRecord(): Boolean = recordState.value != null

	override suspend fun getAcademicRecord(): AcademicRecord? = recordState.value

	override suspend fun getRecordRevision(): Long? = revision

	override suspend fun saveAcademicRecord(record: VersionedAcademicRecord) {
		revision = record.revision
		recordState.value = record.record
		stateHistory += record.record
		savedRecords += record
	}

	fun overrideGradeTimeline(attemptId: String): List<Int?> {
		return stateHistory.map { record ->
			record.attemptOverrides.firstOrNull { override ->
				override.attemptId == attemptId
			}?.score?.numericValue
		}
	}
}

internal class LaggyAcademicRecordLocalDataRepository(
	private val delegate: FakeAcademicRecordLocalDataRepository,
	private val confirmedLandingLagMillis: Long = DEFAULT_CONFIRMED_LANDING_LAG_MILLIS
) : AcademicRecordLocalDataRepository by delegate {
	override suspend fun saveAcademicRecord(record: VersionedAcademicRecord) {
		delay(confirmedLandingLagMillis)
		delegate.saveAcademicRecord(record)
	}
}

internal class ControlledAcademicRecordRemoteDataRepository(
	private val upsertResponse: VersionedAcademicRecord
) : AcademicRecordRemoteDataRepository {
	val deleteStarted = CompletableDeferred<Unit>()
	val releaseDeleteFailure = CompletableDeferred<Unit>()

	var getRecordCalls = 0
	var upsertAttemptCalls = 0
	var deleteAttemptCalls = 0
	var updateSyntheticTermCalls = 0
	var deleteSyntheticTermCalls = 0
	val upsertAttemptMutationIds = mutableListOf<String>()
	val upsertAttemptExpectedRevisions = mutableListOf<Long>()
	val deleteAttemptMutationIds = mutableListOf<String>()
	val deleteAttemptExpectedRevisions = mutableListOf<Long>()
	val updateSyntheticTermMutationIds = mutableListOf<String>()
	val updateSyntheticTermExpectedRevisions = mutableListOf<Long>()
	val updateSyntheticTermCommands = mutableListOf<AcademicRecordMutation.UpdateSyntheticTerm>()
	val deleteSyntheticTermIds = mutableListOf<String>()
	val deleteSyntheticTermMutationIds = mutableListOf<String>()
	val deleteSyntheticTermExpectedRevisions = mutableListOf<Long>()

	override suspend fun getAcademicRecord(): VersionedAcademicRecord {
		getRecordCalls += 1
		return upsertResponse
	}

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		upsertAttemptCalls += 1
		upsertAttemptMutationIds += mutationId
		upsertAttemptExpectedRevisions += expectedRevision
		return upsertResponse
	}

	override suspend fun deleteAttemptOverride(
		attemptId: String,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		deleteAttemptCalls += 1
		deleteAttemptMutationIds += mutationId
		deleteAttemptExpectedRevisions += expectedRevision
		deleteStarted.complete(Unit)
		releaseDeleteFailure.await()
		throw clientRequestException(
			statusCode = HttpStatusCode.NotFound,
			path = "/record/v5/overlay/attempts/$attemptId"
		)
	}

	override suspend fun addSyntheticTerm(
		command: AcademicRecordMutation.AddSyntheticTerm,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord = upsertResponse

	override suspend fun updateSyntheticTerm(
		command: AcademicRecordMutation.UpdateSyntheticTerm,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		updateSyntheticTermCalls += 1
		updateSyntheticTermCommands += command
		updateSyntheticTermMutationIds += mutationId
		updateSyntheticTermExpectedRevisions += expectedRevision
		return upsertResponse
	}

	override suspend fun deleteSyntheticTerm(
		termId: String,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		deleteSyntheticTermCalls += 1
		deleteSyntheticTermIds += termId
		deleteSyntheticTermMutationIds += mutationId
		deleteSyntheticTermExpectedRevisions += expectedRevision
		return upsertResponse
	}
}

internal class RebasingAcademicRecordRemoteDataRepository(
	private val staleResponse: VersionedAcademicRecord,
	private val latestResponse: VersionedAcademicRecord
) : AcademicRecordRemoteDataRepository {
	val firstUpsertStarted = CompletableDeferred<Unit>()
	val releaseFirstUpsert = CompletableDeferred<Unit>()

	val upsertAttemptMutationIds = mutableListOf<String>()
	val upsertAttemptExpectedRevisions = mutableListOf<Long>()

	private var upsertAttemptCalls = 0

	override suspend fun getAcademicRecord(): VersionedAcademicRecord = staleResponse

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		upsertAttemptCalls += 1
		upsertAttemptMutationIds += mutationId
		upsertAttemptExpectedRevisions += expectedRevision

		return when (upsertAttemptCalls) {
			1 -> {
				firstUpsertStarted.complete(Unit)
				releaseFirstUpsert.await()
				staleResponse
			}

			2 -> throw clientRequestException(
				statusCode = HttpStatusCode.PreconditionFailed,
				path = "/record/v5/overlay/attempts/$attemptId"
			)

			else -> latestResponse
		}
	}

	override suspend fun deleteAttemptOverride(
		attemptId: String,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord = latestResponse

	override suspend fun addSyntheticTerm(
		command: AcademicRecordMutation.AddSyntheticTerm,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord = latestResponse

	override suspend fun updateSyntheticTerm(
		command: AcademicRecordMutation.UpdateSyntheticTerm,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord = latestResponse

	override suspend fun deleteSyntheticTerm(
		termId: String,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord = latestResponse
}

internal class DelayedSequentialUpsertAcademicRecordRemoteDataRepository(
	private val firstResponse: VersionedAcademicRecord,
	private val secondResponse: VersionedAcademicRecord
) : AcademicRecordRemoteDataRepository {
	val firstUpsertStarted = CompletableDeferred<Unit>()
	val releaseFirstUpsert = CompletableDeferred<Unit>()

	private var upsertAttemptCalls = 0

	override suspend fun getAcademicRecord(): VersionedAcademicRecord = secondResponse

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord {
		upsertAttemptCalls += 1
		return when (upsertAttemptCalls) {
			1 -> {
				firstUpsertStarted.complete(Unit)
				releaseFirstUpsert.await()
				firstResponse
			}

			else -> secondResponse
		}
	}

	override suspend fun deleteAttemptOverride(
		attemptId: String,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord = secondResponse

	override suspend fun addSyntheticTerm(
		command: AcademicRecordMutation.AddSyntheticTerm,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord = secondResponse

	override suspend fun updateSyntheticTerm(
		command: AcademicRecordMutation.UpdateSyntheticTerm,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord = secondResponse

	override suspend fun deleteSyntheticTerm(
		termId: String,
		mutationId: String,
		expectedRevision: Long
	): VersionedAcademicRecord = secondResponse
}

internal class FakeRecordSettingsDataRepository(
	private val onCooldown: Boolean = false
) : RecordSettingsDataRepository {
	var cooldownMarked = false

	override suspend fun isGetAcademicRecordOnCooldown(): Boolean = onCooldown

	override suspend fun setGetAcademicRecordOnCooldown() {
		cooldownMarked = true
	}
}

internal class FakeIdentifierRepository : IdentifierRepository {
	private var nextId = 0

	override fun generateRandomIdentifier(): String {
		nextId += 1
		return "mutation-$nextId"
	}
}

internal class InMemoryMutationEnvelopeStore(
	initialMutations: List<MutationEnvelope<String, AcademicRecordMutation>> = emptyList()
) : MutationEnvelopeStore<String, AcademicRecordMutation> {
	private val state = MutableStateFlow(initialMutations)

	override fun observePendingMutations(
		scopeKey: String
	): Flow<List<MutationEnvelope<String, AcademicRecordMutation>>> {
		return state.map { mutations ->
			mutations.filter { mutation ->
				mutation.scopeKey == scopeKey && mutation.status == PendingMutationStatus.Pending
			}
		}
	}

	override suspend fun getPendingMutations(
		scopeKey: String
	): List<MutationEnvelope<String, AcademicRecordMutation>> {
		return state.value.filter { mutation ->
			mutation.scopeKey == scopeKey && mutation.status == PendingMutationStatus.Pending
		}
	}

	override suspend fun requeueFailedMutations(
		scopeKey: String,
		retryableBefore: Long
	): Int {
		var requeued = 0
		state.value = state.value.map { mutation ->
			if (
				mutation.scopeKey == scopeKey &&
				mutation.status == PendingMutationStatus.Failed &&
				mutation.updatedAt <= retryableBefore
			) {
				requeued += 1
				mutation.copy(status = PendingMutationStatus.Pending, lastError = null)
			} else {
				mutation
			}
		}
		return requeued
	}

	override fun observeMutations(
		scopeKey: String
	): Flow<List<MutationEnvelope<String, AcademicRecordMutation>>> {
		return state.map { mutations ->
			mutations.filter { mutation -> mutation.scopeKey == scopeKey }
		}
	}

	override suspend fun getMutations(
		scopeKey: String
	): List<MutationEnvelope<String, AcademicRecordMutation>> {
		return state.value.filter { mutation -> mutation.scopeKey == scopeKey }
	}

	override suspend fun getPendingMutation(
		scopeKey: String,
		mutationId: String
	): MutationEnvelope<String, AcademicRecordMutation>? {
		return state.value.firstOrNull { mutation ->
			mutation.scopeKey == scopeKey && mutation.mutationId == mutationId
		}
	}

	override suspend fun replacePendingMutation(
		mutation: MutationEnvelope<String, AcademicRecordMutation>
	) {
		state.value = state.value
			.filterNot { pending -> pending.replaceKey == mutation.replaceKey }
			.plus(mutation)
	}

	override suspend fun savePendingMutation(
		mutation: MutationEnvelope<String, AcademicRecordMutation>
	) {
		state.value = state.value
			.filterNot { pending -> pending.mutationId == mutation.mutationId }
			.plus(mutation)
	}

	override suspend fun deletePendingMutation(
		scopeKey: String,
		mutationId: String
	) {
		state.value = state.value.filterNot { mutation ->
			mutation.scopeKey == scopeKey && mutation.mutationId == mutationId
		}
	}
}
