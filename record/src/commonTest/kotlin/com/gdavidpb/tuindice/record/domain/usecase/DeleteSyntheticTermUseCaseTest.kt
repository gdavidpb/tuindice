package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.record.domain.model.RecordRejection
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.DeleteSyntheticTermParams
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DeleteSyntheticTermUseCaseTest {
	@Test
	fun executeOnBackground_deletesRequestedTerm() = runTest {
		val repository = RecordingAcademicRecordRepository()
		val useCase = DeleteSyntheticTermUseCase(
			academicRecordRepository = repository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = RecordExceptionHandler()
		)

		useCase.executeOnBackground(DeleteSyntheticTermParams("2027-JUL_AUG")).single()

		assertEquals(listOf("2027-JUL_AUG"), repository.deletedTermIds)
	}
}

private class RecordingAcademicRecordRepository : AcademicRecordRepository {
	override suspend fun observeTerminallyRejectedMutationIdsFlow(): Flow<List<String>> = emptyFlow()

	override suspend fun observeTerminallyRejectedMutationsFlow(): Flow<List<RecordRejection>> = emptyFlow()

	override suspend fun acknowledgeTerminallyRejectedMutations(mutationIds: List<String>) = Unit

	override suspend fun getRecordRevision(): Long? = null

	override fun observeMutationOutcomesFlow(): Flow<MutationOutcome> = emptyFlow()

	val deletedTermIds = mutableListOf<String>()

	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> = emptyFlow()

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> = emptyFlow()

	override suspend fun getAcademicRecord(): AcademicRecord? = null

	override suspend fun updateAcademicRecord() = Unit

	override suspend fun drainPendingMutations() = Unit

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		mutationId: String?
	) = Unit

	override suspend fun deleteAttemptOverride(attemptId: String, mutationId: String?) = Unit

	override suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand) = Unit

	override suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand) = Unit

	override suspend fun deleteSyntheticTerm(termId: String, mutationId: String?) {
		deletedTermIds += termId
	}
}
