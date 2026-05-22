package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.usecase.DeleteSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_synthetic_term_deleted
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class DeleteSyntheticTermActionProcessorTest {
	@Test
	fun process_whenDeleteSucceeds_showsSuccessSnackBar() = runTest {
		val processor = createProcessor(repository = DeleteTermRepository())
		val effects = mutableListOf<Record.Effect>()
		val mutations = processor.process(
			action = Record.Action.DeleteSyntheticTerm("2027-JUL_AUG"),
			sideEffect = effects::add
		).toList()

		var state: Record.State = Record.State.Loading
		mutations.forEach { mutation ->
			state = mutation(state)
		}

		assertEquals(Record.State.Loading, state)
		val effect = assertIs<Record.Effect.ShowSnackBar>(effects.single())
		assertEquals(getString(Res.string.snack_synthetic_term_deleted), effect.message)
	}

	@Test
	fun process_whenUnauthorized_navigatesToOutdatedCredentials() = runTest {
		val processor = createProcessor(
			repository = DeleteTermRepository(
				throwable = clientRequestException(
					statusCode = HttpStatusCode.Unauthorized,
					path = "/record/v5/overlay/terms/2027-JUL_AUG"
				)
			)
		)
		val effects = mutableListOf<Record.Effect>()
		val mutations = processor.process(
			action = Record.Action.DeleteSyntheticTerm("2027-JUL_AUG"),
			sideEffect = effects::add
		).toList()

		var state: Record.State = Record.State.Loading
		mutations.forEach { mutation ->
			state = mutation(state)
		}

		assertEquals(Record.State.Loading, state)
		assertIs<Record.Effect.NavigateToOutdatedCredentials>(effects.first())
		assertIs<Record.Effect.ShowSnackBar>(effects.last())
	}

	private fun createProcessor(
		repository: DeleteTermRepository
	): DeleteSyntheticTermActionProcessor {
		return DeleteSyntheticTermActionProcessor(
			deleteSyntheticTermUseCase = DeleteSyntheticTermUseCase(
				academicRecordRepository = repository,
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = RecordExceptionHandler()
			)
		)
	}
}

private class DeleteTermRepository(
	private val throwable: Throwable? = null
) : AcademicRecordRepository {
	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> = emptyFlow()

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> = emptyFlow()

	override suspend fun getAcademicRecord(): AcademicRecord? = null

	override suspend fun updateAcademicRecord() = Unit

	override suspend fun drainPendingMutations() = Unit

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		commit: Boolean
	) = Unit

	override suspend fun deleteAttemptOverride(attemptId: String) = Unit

	override suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand) = Unit

	override suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand) = Unit

	override suspend fun deleteSyntheticTerm(termId: String) {
		throwable?.let { throw it }
	}
}
