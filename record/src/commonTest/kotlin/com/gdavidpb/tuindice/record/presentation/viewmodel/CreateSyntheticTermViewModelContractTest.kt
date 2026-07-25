package com.gdavidpb.tuindice.record.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationSnapshot
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.usecase.CreateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.LoadSyntheticTermEditSeedUseCase
import com.gdavidpb.tuindice.record.domain.usecase.LoadSyntheticTermPreviewUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveSyntheticTermCreationUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RefreshSyntheticTermSubjectSearchUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.machine.CreateSyntheticTermDraft
import com.gdavidpb.tuindice.record.presentation.machine.CreateSyntheticTermMachine
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.ControllableSyntheticTermCreationRepository
import com.gdavidpb.tuindice.record.testing.FakeSyntheticTermLoadPreviewRepository
import com.gdavidpb.tuindice.record.testing.RecordingRecordSelectionRepository
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest

class CreateSyntheticTermViewModelContractTest {
	@Test
	fun updateQuery_preservesCursorSelection() = runTest {
		val fixture = createFixture()

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				assertEquals(CreateSyntheticTerm.State(), awaitItem())

				fixture.viewModel.updateQueryAction(
					query = "ma",
					selectionStart = 1,
					selectionEnd = 2
				)

				val updated = awaitUntilState<CreateSyntheticTerm.State> { state ->
					state.query == "ma"
				}
				assertEquals(1, updated.querySelectionStart)
				assertEquals(2, updated.querySelectionEnd)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun updateQuery_clampsSelectionAndClearsResultsForShortQuery() = runTest {
		val fixture = createFixture()

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				assertEquals(CreateSyntheticTerm.State(), awaitItem())

				fixture.viewModel.updateQueryAction(
					query = "a",
					selectionStart = 5,
					selectionEnd = 9
				)

				val updated = awaitUntilState<CreateSyntheticTerm.State> { state ->
					state.query == "a"
				}
				assertEquals(1, updated.querySelectionStart)
				assertEquals(1, updated.querySelectionEnd)
				assertTrue(updated.searchResults.isEmpty())
				assertEquals(false, updated.isRefreshingSearch)
				assertEquals(false, updated.hasSearchError)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun createTerm_submits_selectsProjectionTerm_andNavigatesBack() = runTest {
		val period = SyntheticTermPeriodOption(
			periodYear = 9999,
			periodCode = AcademicTermPeriod.JAN_MAR
		)
		val subject = SyntheticTermSubject(
			subjectCode = "MA1112",
			name = "MA1112",
			credits = 4
		)
		val fixture = createFixture(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "historical",
						kind = TermKind.HISTORICAL,
						attempts = listOf(
							academicAttempt(
								subjectCode = "MA1112",
								outcome = AttemptOutcome.FAILED
							)
						)
					)
				)
			)
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.creationRepository.snapshotFlow.value = SyntheticTermCreationSnapshot(
				periodOptions = listOf(period),
				selectedPeriod = period,
				selectedSubjects = listOf(subject),
				suggestedSubjects = emptyList(),
				searchResults = emptyList()
			)

			fixture.viewModel.state.test {
				awaitUntilState<CreateSyntheticTerm.State> { state -> state.canSubmit }

				cancelAndIgnoreRemainingEvents()
			}

			fixture.viewModel.effect.test {
				fixture.viewModel.createTermAction()

				assertIs<CreateSyntheticTerm.Effect.NavigateBack>(awaitItem())

				cancelAndIgnoreRemainingEvents()
			}

			assertEquals(1, fixture.academicRecordRepository.addedTerms.size)
			assertEquals(
				RecordViewMode.Projection to period.termKey,
				fixture.selectionRepository.setSelectedTermCalls.last()
			)
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun createTerm_whileSubmitting_isIgnored() = runTest {
		val period = SyntheticTermPeriodOption(
			periodYear = 9999,
			periodCode = AcademicTermPeriod.JAN_MAR
		)
		val subject = SyntheticTermSubject(
			subjectCode = "MA1112",
			name = "MA1112",
			credits = 4
		)
		val fixture = createFixture(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "historical",
						kind = TermKind.HISTORICAL,
						attempts = listOf(
							academicAttempt(
								subjectCode = "MA1112",
								outcome = AttemptOutcome.FAILED
							)
						)
					)
				)
			)
		)
		val gate = CompletableDeferred<Unit>()
		fixture.academicRecordRepository.addSyntheticTermGate = gate

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.creationRepository.snapshotFlow.value = SyntheticTermCreationSnapshot(
				periodOptions = listOf(period),
				selectedPeriod = period,
				selectedSubjects = listOf(subject),
				suggestedSubjects = emptyList(),
				searchResults = emptyList()
			)

			fixture.viewModel.state.test {
				awaitUntilState<CreateSyntheticTerm.State> { state -> state.canSubmit }

				fixture.viewModel.createTermAction()

				awaitUntilState<CreateSyntheticTerm.State> { state -> state.isSubmitting }

				// While a submit is in flight, canSubmit is false and a second click is
				// ignored by the EFSM guard instead of launching a parallel submit.
				fixture.viewModel.createTermAction()

				gate.complete(Unit)

				awaitUntilState<CreateSyntheticTerm.State> { state -> !state.isSubmitting }

				cancelAndIgnoreRemainingEvents()
			}

			assertEquals(1, fixture.academicRecordRepository.addedTerms.size)
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun editTerm_whenSeedArrivesAfterTheTermIsConfigured_keepsSubmitDisabled() = runTest {
		val period = SyntheticTermPeriodOption(
			periodYear = 9999,
			periodCode = AcademicTermPeriod.JAN_MAR
		)
		val seededSubject = SyntheticTermSubject(
			subjectCode = "MA1112",
			name = "MA1112",
			credits = 4
		)
		val fixture = createFixture()
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		fun editSnapshot(subjects: List<SyntheticTermSubject>) = SyntheticTermCreationSnapshot(
			editingTermId = "synthetic-term",
			editingTermKey = period.termKey,
			periodOptions = listOf(period),
			selectedPeriod = period,
			selectedSubjects = subjects,
			suggestedSubjects = emptyList(),
			searchResults = emptyList()
		)

		try {
			fixture.viewModel.state.test {
				fixture.creationRepository.snapshotFlow.value = editSnapshot(subjects = emptyList())
				awaitUntilState<CreateSyntheticTerm.State> { state -> state.isEditing }

				fixture.creationRepository.snapshotFlow.value = editSnapshot(
					subjects = listOf(seededSubject)
				)
				val seeded = awaitUntilState<CreateSyntheticTerm.State> { state ->
					state.selectedSubjects.size == 1
				}

				assertEquals(listOf("MA1112"), seeded.initialDraft?.subjectCodes)
				assertEquals(false, seeded.canSubmit)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun editTerm_disablesSubmitUntilDraftChanges() = runTest {
		val period = SyntheticTermPeriodOption(
			periodYear = 9999,
			periodCode = AcademicTermPeriod.JAN_MAR
		)
		val originalSubject = SyntheticTermSubject(
			subjectCode = "MA1112",
			name = "MA1112",
			credits = 4
		)
		val addedSubject = SyntheticTermSubject(
			subjectCode = "CI2125",
			name = "CI2125",
			credits = 4
		)
		val fixture = createFixture()
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		fun editSnapshot(subjects: List<SyntheticTermSubject>) = SyntheticTermCreationSnapshot(
			editingTermId = "synthetic-term",
			editingTermKey = period.termKey,
			periodOptions = listOf(period),
			selectedPeriod = period,
			selectedSubjects = subjects,
			suggestedSubjects = emptyList(),
			searchResults = emptyList()
		)

		try {
			fixture.viewModel.state.test {
				fixture.creationRepository.snapshotFlow.value = editSnapshot(
					subjects = listOf(originalSubject)
				)
				val loaded = awaitUntilState<CreateSyntheticTerm.State> { state ->
					state.isEditing && state.initialDraft != null
				}
				assertEquals(false, loaded.canSubmit)

				fixture.creationRepository.snapshotFlow.value = editSnapshot(
					subjects = listOf(originalSubject, addedSubject)
				)
				val changed = awaitUntilState<CreateSyntheticTerm.State> { state ->
					state.canSubmit
				}
				assertEquals(
					listOf("MA1112", "CI2125"),
					changed.draft.subjectCodes
				)

				fixture.creationRepository.snapshotFlow.value = editSnapshot(
					subjects = listOf(originalSubject)
				)
				val restored = awaitUntilState<CreateSyntheticTerm.State> { state ->
					!state.canSubmit && state.selectedSubjects.size == 1
				}
				assertEquals(false, restored.canSubmit)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun createTerm_validationError_setsSubmitError() = runTest {
		val period = SyntheticTermPeriodOption(
			periodYear = 9999,
			periodCode = AcademicTermPeriod.JAN_MAR
		)
		val subject = SyntheticTermSubject(
			subjectCode = "MA1112",
			name = "MA1112",
			credits = 4
		)
		val fixture = createFixture(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "existing",
						kind = TermKind.SYNTHETIC,
						periodYear = 9999,
						periodCode = AcademicTermPeriod.JAN_MAR
					)
				)
			)
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.creationRepository.snapshotFlow.value = SyntheticTermCreationSnapshot(
				periodOptions = listOf(period),
				selectedPeriod = period,
				selectedSubjects = listOf(subject),
				suggestedSubjects = emptyList(),
				searchResults = emptyList()
			)

			fixture.viewModel.state.test {
				awaitUntilState<CreateSyntheticTerm.State> { state -> state.canSubmit }

				fixture.viewModel.createTermAction()

				val failed = awaitUntilState<CreateSyntheticTerm.State> { state ->
					state.submitError != UiText.Empty
				}
				assertEquals(false, failed.isSubmitting)

				cancelAndIgnoreRemainingEvents()
			}

			assertTrue(fixture.academicRecordRepository.addedTerms.isEmpty())
		} finally {
			stateCollector.cancel()
		}
	}

	private fun createFixture(
		record: AcademicRecord = AcademicRecord(id = "record")
	): CreateSyntheticTermFixture {
		val academicRecordRepository = ControllableAcademicRecordRepository(
			initialRecord = record,
			initialHasSynced = true
		)
		val selectionRepository = RecordingRecordSelectionRepository()
		val creationRepository = ControllableSyntheticTermCreationRepository()
		val reportingRepository = RecordingReportingRepository()
		val exceptionHandler = RecordExceptionHandler()

		val viewModel = CreateSyntheticTermViewModel(
			screenMachine = CreateSyntheticTermMachine(
				draft = CreateSyntheticTermDraft(),
				observeSyntheticTermCreationUseCase = ObserveSyntheticTermCreationUseCase(
					repository = creationRepository,
					reportingRepository = reportingRepository
				),
				refreshSyntheticTermSubjectSearchUseCase = RefreshSyntheticTermSubjectSearchUseCase(
					repository = creationRepository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				loadSyntheticTermPreviewUseCase = LoadSyntheticTermPreviewUseCase(
					repository = FakeSyntheticTermLoadPreviewRepository(),
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				loadSyntheticTermEditSeedUseCase = LoadSyntheticTermEditSeedUseCase(
					repository = academicRecordRepository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				createSyntheticTermUseCase = CreateSyntheticTermUseCase(
					repository = academicRecordRepository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				updateSyntheticTermUseCase = UpdateSyntheticTermUseCase(
					repository = academicRecordRepository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				setSelectedTermUseCase = SetSelectedTermUseCase(
					recordSelectionRepository = selectionRepository,
					reportingRepository = reportingRepository
				)
			),
			eventPublisher = NoOpEventPublisher
		)

		return CreateSyntheticTermFixture(
			viewModel = viewModel,
			academicRecordRepository = academicRecordRepository,
			selectionRepository = selectionRepository,
			creationRepository = creationRepository
		)
	}
}

private data class CreateSyntheticTermFixture(
	val viewModel: CreateSyntheticTermViewModel,
	val academicRecordRepository: ControllableAcademicRecordRepository,
	val selectionRepository: RecordingRecordSelectionRepository,
	val creationRepository: ControllableSyntheticTermCreationRepository
)
