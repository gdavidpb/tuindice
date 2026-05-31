package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import com.gdavidpb.tuindice.record.domain.usecase.CreateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_error_subject_already_taken

class CreateSyntheticTermActionProcessorTest {
	@Test
	fun process_whenCreateSucceeds_selectsCreatedProjectionTermAndNavigatesBack() = runTest {
		val repository = RecordingAcademicRecordRepository(
			record = AcademicRecord(
				id = "record",
				terms = emptyList()
			)
		)
		val selectionRepository = RecordingCreateTermSelectionRepository()
		val effects = mutableListOf<CreateSyntheticTerm.Effect>()
		val mutations = processor(
			repository = repository,
			selectionRepository = selectionRepository
		).process(
			action = CreateSyntheticTerm.Action.CreateTerm(
				editingTermId = null,
				editingTermKey = null,
				period = SyntheticTermPeriodOption(
					periodYear = 9999,
					periodCode = AcademicTermPeriod.JAN_MAR
				),
				subjects = listOf(
					SyntheticTermSubject(
						subjectCode = "MA1112",
						name = "Matemáticas II",
						credits = 4
					)
				)
			),
			sideEffect = { effect -> effects += effect }
		).toList()

		var state = CreateSyntheticTerm.State(isSubmitting = true)
		for (mutation in mutations) {
			state = mutation(state)
		}

		assertEquals(false, state.isSubmitting)
		assertEquals("9999-JAN_MAR", repository.addedTerms.single().termId)
		assertEquals(
			"9999-JAN_MAR",
			selectionRepository.selectedTermIds.getValue(RecordViewMode.Projection)
		)
		assertEquals(1, effects.size)
		assertEquals(CreateSyntheticTerm.Effect.NavigateBack, effects.single())
	}

	@Test
	fun process_whenLocalValidationFails_keepsUserInCreateFlowAndShowsSubmitError() = runTest {
		val repository = RecordingAcademicRecordRepository(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					AcademicTerm(
						id = "historical",
						periodYear = 2024,
						periodCode = AcademicTermPeriod.JAN_MAR,
						kind = TermKind.HISTORICAL,
						attempts = listOf(
							AcademicAttempt(
								id = "attempt-ma1112",
								subjectCode = "MA1112",
								subjectName = "Matemáticas II",
								credits = 4,
								academicOutcome = AttemptOutcome.APPROVED
							)
						)
					)
				)
			)
		)
		val effects = mutableListOf<CreateSyntheticTerm.Effect>()
		val mutations = processor(repository).process(
			action = CreateSyntheticTerm.Action.CreateTerm(
				editingTermId = null,
				editingTermKey = null,
				period = SyntheticTermPeriodOption(
					periodYear = 9999,
					periodCode = AcademicTermPeriod.JAN_MAR
				),
				subjects = listOf(
					SyntheticTermSubject(
						subjectCode = "MA1112",
						name = "Matemáticas II",
						credits = 4
					)
				)
			),
			sideEffect = { effect -> effects += effect }
		).toList()

		var state = CreateSyntheticTerm.State()
		for (mutation in mutations) {
			state = mutation(state)
		}

		assertEquals(
			UiText.Resource(Res.string.create_term_error_subject_already_taken),
			state.submitError
		)
		assertEquals(emptyList(), repository.addedTerms)
		assertTrue(effects.isEmpty())
	}

	private fun processor(
		repository: AcademicRecordRepository,
		selectionRepository: RecordSelectionRepository = RecordingCreateTermSelectionRepository()
	): CreateSyntheticTermActionProcessor {
		val reportingRepository = RecordingReportingRepository()
		val exceptionHandler = RecordExceptionHandler()
		return CreateSyntheticTermActionProcessor(
			createSyntheticTermUseCase = CreateSyntheticTermUseCase(
				repository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			updateSyntheticTermUseCase = UpdateSyntheticTermUseCase(
				repository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			setSelectedTermUseCase = SetSelectedTermUseCase(
				recordSelectionRepository = selectionRepository,
				reportingRepository = reportingRepository
			)
		)
	}
}

private class RecordingAcademicRecordRepository(
	private val record: AcademicRecord?
) : AcademicRecordRepository {
	val addedTerms = mutableListOf<SyntheticTermCreationCommand>()

	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> = emptyFlow()

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> = emptyFlow()

	override suspend fun getAcademicRecord(): AcademicRecord? = record

	override suspend fun updateAcademicRecord() = Unit

	override suspend fun drainPendingMutations() = Unit

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?,
		commit: Boolean
	) = Unit

	override suspend fun deleteAttemptOverride(attemptId: String) = Unit

	override suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand) {
		addedTerms += command
	}

	override suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand) = Unit

	override suspend fun deleteSyntheticTerm(termId: String) = Unit
}

private class RecordingCreateTermSelectionRepository : RecordSelectionRepository {
	val selectedTermIds = mutableMapOf<RecordViewMode, String>()

	override fun observeSelectedTermId(viewMode: RecordViewMode): Flow<String?> {
		return flowOf(selectedTermIds[viewMode])
	}

	override fun observeRecordViewMode(): Flow<RecordViewMode> = flowOf(RecordViewMode.Projection)

	override suspend fun getSelectedTermId(viewMode: RecordViewMode): String? {
		return selectedTermIds[viewMode]
	}

	override suspend fun setSelectedTermId(viewMode: RecordViewMode, termId: String) {
		selectedTermIds[viewMode] = termId
	}

	override suspend fun getRecordViewMode(): RecordViewMode = RecordViewMode.Projection

	override suspend fun setRecordViewMode(viewMode: RecordViewMode) = Unit
}
