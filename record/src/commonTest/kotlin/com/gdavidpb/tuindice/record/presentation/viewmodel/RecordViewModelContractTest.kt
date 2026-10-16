package com.gdavidpb.tuindice.record.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.data.source.InMemoryRecordAttemptFocusDataSource
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.EnrollmentSituation
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncReportSources
import com.gdavidpb.tuindice.base.domain.model.SyncReportStatus
import com.gdavidpb.tuindice.base.domain.model.SyncSourceReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.presentation.mapper.EnrollmentAnnulmentTexts
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.usecase.DeleteSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.EnsureRecordLoadedUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveNewStudentNoRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveSyntheticTermRejectionsUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpsertAttemptSelectionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine
import com.gdavidpb.tuindice.record.presentation.model.RecordNoticeKind
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.RecordingRecordSelectionRepository
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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
	fun ensureLoaded_whenContentExists_keepsContentDuringInitialRefresh() = runTest {
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
			hasSynced = true,
			dispatchers = TestTuIndiceDispatchers(UnconfinedTestDispatcher(testScheduler))
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				awaitUntilState<Record.State.Content>()

				fixture.viewModel.ensureRecordLoadedAction()
				advanceUntilIdle()

				expectNoEvents()
				cancelAndIgnoreRemainingEvents()
			}

			assertEquals(1, fixture.academicRecordRepository.updateAcademicRecordCalls)
			assertEquals(listOf(false), fixture.academicRecordRepository.updateAcademicRecordForceRemoteCalls)
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun ensureLoaded_whenCacheExistsBeforeObservation_doesNotShowLoadingDuringInitialRefresh() = runTest {
		val cachedRecord = AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(
					id = "term",
					attempts = listOf(academicAttempt(subjectCode = "MAT101"))
				)
			)
		)
		val observedRecords = MutableSharedFlow<AcademicRecord>()
		val fixture = createFixture(
			record = AcademicRecord(id = "empty"),
			hasSynced = true,
			dispatchers = TestTuIndiceDispatchers(UnconfinedTestDispatcher(testScheduler)),
			observedRecordFlow = observedRecords,
			cachedRecord = cachedRecord
		)

		fixture.viewModel.state.test {
			assertEquals(Record.State.Idle, awaitItem())

			fixture.viewModel.ensureRecordLoadedAction()
			advanceUntilIdle()
			expectNoEvents()

			observedRecords.emit(cachedRecord)
			assertIs<Record.State.Content>(awaitItem())
			cancelAndIgnoreRemainingEvents()
		}

		assertEquals(1, fixture.academicRecordRepository.updateAcademicRecordCalls)
		assertEquals(listOf(false), fixture.academicRecordRepository.updateAcademicRecordForceRemoteCalls)
	}

	@Test
	fun ensureLoaded_whenCacheExistsAndInitialRefreshFails_doesNotShowFailedBeforeCachedContent() = runTest {
		val cachedRecord = AcademicRecord(
			id = "record",
			terms = listOf(
				academicTerm(
					id = "term",
					attempts = listOf(academicAttempt(subjectCode = "MAT101"))
				)
			)
		)
		val observedRecords = MutableSharedFlow<AcademicRecord>()
		val fixture = createFixture(
			record = AcademicRecord(id = "empty"),
			hasSynced = true,
			dispatchers = TestTuIndiceDispatchers(UnconfinedTestDispatcher(testScheduler)),
			observedRecordFlow = observedRecords,
			cachedRecord = cachedRecord
		)
		fixture.academicRecordRepository.updateAcademicRecordThrowable =
			RuntimeException("network unavailable")

		fixture.viewModel.state.test {
			assertEquals(Record.State.Idle, awaitItem())

			fixture.viewModel.ensureRecordLoadedAction()
			advanceUntilIdle()
			expectNoEvents()

			observedRecords.emit(cachedRecord)
			assertIs<Record.State.Content>(awaitItem())
			cancelAndIgnoreRemainingEvents()
		}

		assertEquals(1, fixture.academicRecordRepository.updateAcademicRecordCalls)
		assertEquals(listOf(false), fixture.academicRecordRepository.updateAcademicRecordForceRemoteCalls)
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

				// The screen follows the choice at once; the store is written from another coroutine, so the test
				// waits for the write itself before it reads what was saved.
				awaitUntilState<Record.State.Content> { state ->
					state.selectedTermId == otherTermId
				}
				fixture.selectionRepository.observeSelectedTermId(RecordViewMode.Historical).first { termId ->
					termId == otherTermId
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
	fun observe_whenAnnulmentComesWithCurrentTerm_contentCarriesProvisionalNotice() = runTest {
		val fixture = createFixture(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "current",
						kind = TermKind.CURRENT,
						attempts = listOf(academicAttempt(subjectCode = "MAT101"))
					)
				)
			),
			hasSynced = true,
			viewMode = RecordViewMode.Projection
		)
		fixture.syncStatusRepository.setSyncReport(annulledReport())

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				val content = awaitUntilState<Record.State.Content>()

				assertEquals(RecordNoticeKind.AnnulledProvisional, content.notice?.kind)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun observe_whenAnnulmentComesWithoutCurrentTerm_contentCarriesFinalNotice() = runTest {
		val fixture = createFixture(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "past",
						attempts = listOf(academicAttempt(subjectCode = "MAT101"))
					)
				)
			),
			hasSynced = true
		)
		fixture.syncStatusRepository.setSyncReport(annulledReport())

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				val content = awaitUntilState<Record.State.Content>()

				assertEquals(RecordNoticeKind.AnnulledFinal, content.notice?.kind)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun observe_whenEmptyAndAnnulled_emptySaysTheFinalAnnulment() = runTest {
		val fixture = createFixture(
			record = AcademicRecord(id = "record"),
			hasSynced = true
		)
		fixture.syncStatusRepository.setSyncReport(annulledReport())

		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				val empty = awaitUntilState<Record.State.Empty>()

				assertEquals(EnrollmentAnnulmentTexts.title(isProvisional = false), empty.title)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	private fun annulledReport(): SyncReport {
		return SyncReport(
			status = SyncReportStatus.Success,
			sources = SyncReportSources(
				record = SyncSourceReport(SyncSourceStatus.Success),
				enrollment = SyncSourceReport(
					status = SyncSourceStatus.Success,
					situation = EnrollmentSituation(code = "01")
				)
			)
		)
	}

	@Test
	fun attemptFocus_fromContent_selectsTheTermThatHoldsTheAttempt_withoutSavingIt() = runTest {
		val fixture = createFixture(
			record = recordWithTwoTerms(),
			hasSynced = true,
			viewMode = RecordViewMode.Projection
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				val initial = awaitUntilState<Record.State.Content>()
				val target = if (initial.selectedTermId == TERM_A) ATTEMPT_B else ATTEMPT_A
				val targetTerm = if (target == ATTEMPT_A) TERM_A else TERM_B
				val savedBefore = fixture.selectionRepository.setSelectedTermCalls.size

				fixture.attemptFocus.request(attemptId = target)

				val content = awaitUntilState<Record.State.Content> { state -> state.arrivalAttemptId == target }
				assertEquals(targetTerm, content.selectedTermId)
				// A jump is navigation, not a preference: nothing is written.
				assertEquals(savedBefore, fixture.selectionRepository.setSelectedTermCalls.size)

				fixture.viewModel.attemptArrivalTakenAction(attemptId = target)

				awaitUntilState<Record.State.Content> { state -> state.arrivalAttemptId == null }
				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun attemptFocus_whileEmpty_isDropped_soItDoesNotApplyWhenContentComesLater() = runTest {
		val fixture = createFixture(
			record = AcademicRecord(id = "record"),
			hasSynced = true,
			viewMode = RecordViewMode.Projection
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				awaitUntilState<Record.State.Empty>()

				fixture.attemptFocus.request(attemptId = ATTEMPT_B)
				// The request is consumed by the empty state before any content exists.
				fixture.attemptFocus.pending.test {
					while (awaitItem() != null) Unit
					cancelAndIgnoreRemainingEvents()
				}

				fixture.academicRecordRepository.recordFlow.value = recordWithTwoTerms()

				val content = awaitUntilState<Record.State.Content>()
				assertNull(content.arrivalAttemptId)
				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun attemptFocus_ofAnAttemptTheRecordDoesNotShow_isTakenAndChangesNothing() = runTest {
		val fixture = createFixture(
			record = recordWithTwoTerms(),
			hasSynced = true,
			viewMode = RecordViewMode.Projection
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				val initial = awaitUntilState<Record.State.Content>()

				fixture.attemptFocus.request(attemptId = "attempt-ghost")
				fixture.attemptFocus.pending.test {
					while (awaitItem() != null) Unit
					cancelAndIgnoreRemainingEvents()
				}

				val afterwards = assertIs<Record.State.Content>(fixture.viewModel.state.value)
				assertEquals(initial.selectedTermId, afterwards.selectedTermId)
				assertNull(afterwards.arrivalAttemptId)
				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	private fun createFixture(
		record: AcademicRecord,
		hasSynced: Boolean,
		viewMode: RecordViewMode = RecordViewMode.Historical,
		dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers,
		observedRecordFlow: Flow<AcademicRecord>? = null,
		cachedRecord: AcademicRecord? = null
	): RecordFixture {
		val academicRecordRepository = ControllableAcademicRecordRepository(
			initialRecord = record,
			initialHasSynced = hasSynced,
			observedRecordFlow = observedRecordFlow,
			cachedRecord = cachedRecord
		)
		val selectionRepository = RecordingRecordSelectionRepository(initialViewMode = viewMode)
		val syncStatusRepository = FakeSyncStatusRepository()
		val attemptFocus = InMemoryRecordAttemptFocusDataSource()

		val viewModel = RecordViewModel(
			screenMachine = createMachine(
				academicRecordRepository = academicRecordRepository,
				selectionRepository = selectionRepository,
				syncStatusRepository = syncStatusRepository,
				attemptFocus = attemptFocus
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)

		return RecordFixture(
			viewModel = viewModel,
			academicRecordRepository = academicRecordRepository,
			selectionRepository = selectionRepository,
			syncStatusRepository = syncStatusRepository,
			attemptFocus = attemptFocus
		)
	}

	private fun createMachine(
		academicRecordRepository: ControllableAcademicRecordRepository,
		selectionRepository: RecordingRecordSelectionRepository,
		syncStatusRepository: FakeSyncStatusRepository,
		attemptFocus: InMemoryRecordAttemptFocusDataSource
	): RecordMachine {
		val reportingRepository = RecordingReportingRepository()
		val exceptionHandler = RecordExceptionHandler()

		return RecordMachine(
			observeRecordUseCase = ObserveRecordUseCase(
				academicRecordRepository = academicRecordRepository,
				recordSelectionRepository = selectionRepository,
				syncStatusRepository = syncStatusRepository,
				reportingRepository = reportingRepository
			),
			observeNewStudentNoRecordUseCase = ObserveNewStudentNoRecordUseCase(
				syncStatusRepository = syncStatusRepository,
				reportingRepository = reportingRepository
			),
			observeSyntheticTermRejectionsUseCase = ObserveSyntheticTermRejectionsUseCase(
				academicRecordRepository = academicRecordRepository,
				reportingRepository = reportingRepository
			),
			ensureRecordLoadedUseCase = EnsureRecordLoadedUseCase(
				academicRecordRepository = academicRecordRepository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
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
			),
			attemptFocusRepository = attemptFocus
		)
	}
}

private data class RecordFixture(
	val viewModel: RecordViewModel,
	val academicRecordRepository: ControllableAcademicRecordRepository,
	val selectionRepository: RecordingRecordSelectionRepository,
	val syncStatusRepository: FakeSyncStatusRepository,
	val attemptFocus: InMemoryRecordAttemptFocusDataSource
)

private const val TERM_A = "term-a"
private const val TERM_B = "term-b"
private const val ATTEMPT_A = "attempt-a"
private const val ATTEMPT_B = "attempt-b"

// Two terms of different years: term A is the one with the subject MAT101, term B the one with FIS101.
private fun recordWithTwoTerms(): AcademicRecord = AcademicRecord(
	id = "record",
	terms = listOf(
		academicTerm(
			id = TERM_A,
			periodYear = 2024,
			attempts = listOf(academicAttempt(subjectCode = "MAT101", id = ATTEMPT_A))
		),
		academicTerm(
			id = TERM_B,
			periodYear = 2023,
			attempts = listOf(academicAttempt(subjectCode = "FIS101", id = ATTEMPT_B))
		)
	)
)
