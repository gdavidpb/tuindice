package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.record.domain.model.RecordRejection
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.DeleteSyntheticTermParams
import com.gdavidpb.tuindice.record.domain.usecase.param.UpsertAttemptSelectionParams
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// The write use cases hand the caller's mutation id to the repository, which keys the outbox row by it.
class RecordMutationIdPropagationTest {
	@Test
	fun upsertAttemptSelection_propagatesTheRequestedIdToTheOverrideWrite() = runTest {
		val repository = RecordingMutationRepository(record = null)

		upsertSelectionUseCase(repository).executeOnBackground(
			UpsertAttemptSelectionParams(
				attemptId = "attempt-1",
				grade = 5,
				outcome = null,
				mutationId = "external-upsert"
			)
		).single()

		assertEquals(listOf(RecordedMutationCall("attempt-1", "external-upsert")), repository.upserts)
		assertEquals(emptyList(), repository.deletedAttempts)
	}

	@Test
	fun upsertAttemptSelection_clearingAnOverride_propagatesTheRequestedIdToTheDeletion() = runTest {
		val repository = RecordingMutationRepository(record = academicRecordWithAttempt(academicScore = 4))

		upsertSelectionUseCase(repository).executeOnBackground(
			UpsertAttemptSelectionParams(
				attemptId = "attempt-1",
				grade = 4,
				outcome = null,
				mutationId = "external-delete-override"
			)
		).single()

		assertEquals(listOf(RecordedMutationCall("attempt-1", "external-delete-override")), repository.deletedAttempts)
		assertEquals(emptyList(), repository.upserts)
	}

	@Test
	fun deleteSyntheticTerm_propagatesTheRequestedIdToTheRepository() = runTest {
		val repository = RecordingMutationRepository(record = null)
		val useCase = DeleteSyntheticTermUseCase(
			academicRecordRepository = repository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = RecordExceptionHandler()
		)

		useCase.executeOnBackground(
			DeleteSyntheticTermParams(termId = "syn-1", mutationId = "external-delete-term")
		).single()

		assertEquals(listOf(RecordedMutationCall("syn-1", "external-delete-term")), repository.deletedTerms)
	}

	private fun upsertSelectionUseCase(repository: AcademicRecordRepository) = UpsertAttemptSelectionUseCase(
		academicRecordRepository = repository,
		reportingRepository = RecordingReportingRepository(),
		exceptionHandler = RecordExceptionHandler()
	)

	private fun academicRecordWithAttempt(academicScore: Int): AcademicRecord {
		return AcademicRecord(
			id = "record-1",
			terms = listOf(
				AcademicTerm(
					id = "term-1",
					periodYear = 2026,
					periodCode = AcademicTermPeriod.JAN_MAR,
					kind = TermKind.CURRENT,
					attempts = listOf(
						AcademicAttempt(
							id = "attempt-1",
							subjectCode = "PB5611",
							subjectName = "Probabilidad",
							credits = 10,
							academicScore = AttemptScore.numeric(academicScore),
							academicOutcome = AttemptOutcome.APPROVED
						)
					)
				)
			)
		)
	}
}

private class RecordingMutationRepository(
	private val record: AcademicRecord?
) : AcademicRecordRepository {
	override suspend fun observeTerminallyRejectedMutationIdsFlow(): Flow<List<String>> = emptyFlow()

	override suspend fun observeTerminallyRejectedMutationsFlow(): Flow<List<RecordRejection>> = emptyFlow()

	override suspend fun acknowledgeTerminallyRejectedMutations(mutationIds: List<String>) = Unit

	override suspend fun getRecordRevision(): Long? = null

	override fun observeMutationOutcomesFlow(): Flow<MutationOutcome> = emptyFlow()

	val upserts = mutableListOf<RecordedMutationCall>()
	val deletedAttempts = mutableListOf<RecordedMutationCall>()
	val deletedTerms = mutableListOf<RecordedMutationCall>()

	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> = emptyFlow()

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> = emptyFlow()

	override suspend fun getAcademicRecord(): AcademicRecord? = record

	override suspend fun updateAcademicRecord() = Unit

	override suspend fun drainPendingMutations() = Unit

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		mutationId: String?
	) {
		upserts += RecordedMutationCall(attemptId, mutationId)
	}

	override suspend fun deleteAttemptOverride(attemptId: String, mutationId: String?) {
		deletedAttempts += RecordedMutationCall(attemptId, mutationId)
	}

	override suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand) = Unit

	override suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand) = Unit

	override suspend fun deleteSyntheticTerm(termId: String, mutationId: String?) {
		deletedTerms += RecordedMutationCall(termId, mutationId)
	}
}

private data class RecordedMutationCall(
	val id: String,
	val mutationId: String?
)
