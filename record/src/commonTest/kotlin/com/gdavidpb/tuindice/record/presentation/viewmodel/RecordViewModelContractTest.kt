package com.gdavidpb.tuindice.record.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.usecase.DeleteSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpsertAttemptSelectionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
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
import kotlinx.coroutines.test.runTest

class RecordViewModelContractTest {
	@Test
	fun observe_resolvesContentWithSelectedTerm() = runTest {
		val fixture = createFixture(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "term",
						attempts = listOf(academicAttempt(subjectCode = "MAT101"))
					)
				)
			),
			hasSynced = false
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				val content = awaitUntilState<Record.State.Content>()
				assertEquals("term", content.selectedTermId)
				assertEquals(RecordViewMode.Historical, content.viewMode)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun observe_keepsLoadingUntilSync_thenReducesToEmpty() = runTest {
		val fixture = createFixture(
			record = AcademicRecord(id = "record"),
			hasSynced = false
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				awaitUntilState<Record.State.Loading>()

				fixture.academicRecordRepository.hasSyncedFlow.value = true

				awaitUntilState<Record.State.Empty>()

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun selectTerm_fromContent_persistsSelectionWithCurrentViewMode() = runTest {
		val fixture = createFixture(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "term-a",
						periodYear = 2024,
						attempts = listOf(academicAttempt(subjectCode = "MAT101"))
					),
					academicTerm(
						id = "term-b",
						periodYear = 2023,
						attempts = listOf(academicAttempt(subjectCode = "FIS101"))
					)
				)
			),
			hasSynced = true
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
			val content = awaitUntilState<Record.State.Content>()
				val otherTermId = if (content.selectedTermId == "term-a") "term-b" else "term-a"

				fixture.viewModel.selectTermAction(termId = otherTermId)

				awaitUntilState<Record.State.Content> { state ->
					state.selectedTermId == otherTermId
				}

				cancelAndIgnoreRemainingEvents()

				assertEquals(
					RecordViewMode.Historical to otherTermId,
					fixture.selectionRepository.setSelectedTermCalls.last()
				)
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun selectTerm_beforeContent_isIgnored() = runTest {
		val fixture = createFixture(
			record = AcademicRecord(id = "record"),
			hasSynced = false
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				awaitUntilState<Record.State.Loading>()

				fixture.viewModel.selectTermAction(termId = "ghost-term")

				// A later observation settles after the rejected selection (FIFO),
				// proving the action was processed without touching the selection.
				fixture.academicRecordRepository.hasSyncedFlow.value = true
				awaitUntilState<Record.State.Empty>()

				cancelAndIgnoreRemainingEvents()
			}

			assertTrue(fixture.selectionRepository.setSelectedTermCalls.isEmpty())
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun setViewMode_emitsTopBarBanner_andPersistsMode() = runTest {
		val fixture = createFixture(
			record = AcademicRecord(id = "record"),
			hasSynced = true
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.effect.test {
				fixture.viewModel.setViewModeAction(RecordViewMode.Projection)

				val banner = assertIs<Record.Effect.ShowTopBarBanner>(awaitItem())
				assertEquals(RecordViewMode.Projection, banner.viewMode)

				cancelAndIgnoreRemainingEvents()
			}

			assertEquals(
				listOf(RecordViewMode.Projection),
				fixture.selectionRepository.setViewModeCalls
			)
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun openEnrollmentProof_fromCurrentProjection_emitsNavigationEffect() = runTest {
		val fixture = createFixture(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "current-term",
						kind = TermKind.CURRENT,
						attempts = listOf(
							academicAttempt(
								subjectCode = "MAT101",
								outcome = AttemptOutcome.PENDING
							)
						)
					)
				)
			),
			hasSynced = true,
			viewMode = RecordViewMode.Projection
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				awaitUntilState<Record.State.Content> { state ->
					state.selectedTermId == "current-term"
				}

				fixture.viewModel.effect.test {
					fixture.viewModel.openEnrollmentProofAction()

					assertIs<Record.Effect.NavigateToEnrollmentProof>(awaitItem())

					cancelAndIgnoreRemainingEvents()
				}

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	private fun createFixture(
		record: AcademicRecord,
		hasSynced: Boolean,
		viewMode: RecordViewMode = RecordViewMode.Historical
	): RecordFixture {
		val academicRecordRepository = ControllableAcademicRecordRepository(
			initialRecord = record,
			initialHasSynced = hasSynced
		)
		val selectionRepository = RecordingRecordSelectionRepository(initialViewMode = viewMode)
		val reportingRepository = RecordingReportingRepository()
		val exceptionHandler = RecordExceptionHandler()

		val viewModel = RecordViewModel(
			screenMachine = RecordMachine(
				observeRecordUseCase = ObserveRecordUseCase(
					academicRecordRepository = academicRecordRepository,
					recordSelectionRepository = selectionRepository,
					reportingRepository = reportingRepository
				),
				updateRecordUseCase = UpdateRecordUseCase(
					academicRecordRepository = academicRecordRepository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				setRecordViewModeUseCase = SetRecordViewModeUseCase(
					recordSelectionRepository = selectionRepository,
					reportingRepository = reportingRepository
				),
				setSelectedTermUseCase = SetSelectedTermUseCase(
					recordSelectionRepository = selectionRepository,
					reportingRepository = reportingRepository
				),
				upsertAttemptSelectionUseCase = UpsertAttemptSelectionUseCase(
					academicRecordRepository = academicRecordRepository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				deleteSyntheticTermUseCase = DeleteSyntheticTermUseCase(
					academicRecordRepository = academicRecordRepository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				)
			),
			eventPublisher = NoOpEventPublisher
		)

		return RecordFixture(
			viewModel = viewModel,
			academicRecordRepository = academicRecordRepository,
			selectionRepository = selectionRepository
		)
	}
}

private data class RecordFixture(
	val viewModel: RecordViewModel,
	val academicRecordRepository: ControllableAcademicRecordRepository,
	val selectionRepository: RecordingRecordSelectionRepository
)
