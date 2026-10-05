package com.gdavidpb.tuindice.record.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.domain.usecase.ObserveScheduleUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetScheduleViewModeUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.machine.ScheduleMachine
import com.gdavidpb.tuindice.record.testing.ControllableAcademicRecordRepository
import com.gdavidpb.tuindice.record.testing.RecordingScheduleSelectionRepository
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleViewModelContractTest {
	@Test
	fun observe_withAScheduledCurrentTerm_resolvesContentOnTheStoredView() = runTest {
		val fixture = createFixture(record = scheduledRecord(), hasSynced = true)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				val content = awaitUntilState<Schedule.State.Content>()

				assertEquals(ScheduleViewMode.Table, content.viewMode)
				assertEquals(listOf("CI5311"), content.schedule.table.rows.map { it.subjectCode })

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun selectView_persistsTheChoice_andContentFollowsIt() = runTest {
		val fixture = createFixture(record = scheduledRecord(), hasSynced = true)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				awaitUntilState<Schedule.State.Content>()

				fixture.viewModel.selectScheduleViewAction(ScheduleViewMode.Week)

				val content = awaitUntilState<Schedule.State.Content> { it.viewMode == ScheduleViewMode.Week }

				assertEquals(ScheduleViewMode.Week, content.viewMode)
				assertEquals(listOf(ScheduleViewMode.Week), fixture.selectionRepository.setViewModeCalls)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun observe_keepsLoadingUntilSync_thenReducesToEmptyWhenNothingIsScheduled() = runTest {
		val fixture = createFixture(record = AcademicRecord(id = "record"), hasSynced = false)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = fixture.viewModel.state,
			testScheduler = testScheduler
		)

		try {
			fixture.viewModel.state.test {
				awaitUntilState<Schedule.State.Loading>()

				fixture.academicRecordRepository.hasSyncedFlow.value = true

				awaitUntilState<Schedule.State.Empty>()

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	private fun scheduledRecord() = AcademicRecord(
		id = "record",
		terms = listOf(
			academicTerm(
				id = "now",
				kind = TermKind.CURRENT,
				attempts = listOf(
					academicAttempt(subjectCode = "CI5311").copy(
							section = 1,
							schedule = listOf(
								AcademicScheduleEntry(dayOfWeek = 2, startBlock = 1, endBlock = 2, classroom = "MYS-116")
							)
						)
				)
			)
		)
	)

	private fun createFixture(record: AcademicRecord, hasSynced: Boolean): ScheduleFixture {
		val academicRecordRepository = ControllableAcademicRecordRepository(
			initialRecord = record,
			initialHasSynced = hasSynced
		)
		val selectionRepository = RecordingScheduleSelectionRepository()
		val reportingRepository = RecordingReportingRepository()

		return ScheduleFixture(
			viewModel = ScheduleViewModel(
				screenMachine = ScheduleMachine(
					observeScheduleUseCase = ObserveScheduleUseCase(
						academicRecordRepository = academicRecordRepository,
						scheduleSelectionRepository = selectionRepository,
						reportingRepository = reportingRepository
					),
					setScheduleViewModeUseCase = SetScheduleViewModeUseCase(
						scheduleSelectionRepository = selectionRepository,
						reportingRepository = reportingRepository
					)
				),
				eventPublisher = NoOpEventPublisher
			),
			academicRecordRepository = academicRecordRepository,
			selectionRepository = selectionRepository
		)
	}

	private class ScheduleFixture(
		val viewModel: ScheduleViewModel,
		val academicRecordRepository: ControllableAcademicRecordRepository,
		val selectionRepository: RecordingScheduleSelectionRepository
	)
}
