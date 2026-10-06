package com.gdavidpb.tuindice.record.testing

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.usecase.CreateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.DeleteSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.EnsureRecordLoadedUseCase
import com.gdavidpb.tuindice.record.domain.usecase.LoadSyntheticTermEditSeedUseCase
import com.gdavidpb.tuindice.record.domain.usecase.LoadSyntheticTermPreviewUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveNewStudentNoRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveScheduleUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveSyntheticTermCreationUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveSyntheticTermRejectionsUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RefreshSyntheticTermSubjectSearchUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetScheduleViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateRecordUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpsertAttemptSelectionUseCase
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.presentation.machine.CreateSyntheticTermDraft
import com.gdavidpb.tuindice.record.presentation.machine.CreateSyntheticTermMachine
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine
import com.gdavidpb.tuindice.record.presentation.machine.ScheduleMachine
import com.gdavidpb.tuindice.record.presentation.viewmodel.CreateSyntheticTermViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.ScheduleViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository

// What a route test needs of the screens behind the routes: the real view model over the real
// machine and use cases, with only the repositories replaced by the doubles a test can drive.

class RecordRouteFixture(
	val viewModel: RecordViewModel,
	val academicRecordRepository: ControllableAcademicRecordRepository,
	val selectionRepository: RecordingRecordSelectionRepository
)

class ScheduleRouteFixture(
	val viewModel: ScheduleViewModel,
	val academicRecordRepository: ControllableAcademicRecordRepository,
	val selectionRepository: RecordingScheduleSelectionRepository
)

class CreateSyntheticTermRouteFixture(
	val viewModel: CreateSyntheticTermViewModel,
	val academicRecordRepository: ControllableAcademicRecordRepository,
	val creationRepository: ControllableSyntheticTermCreationRepository
)

fun recordRouteFixture(
	record: AcademicRecord,
	viewMode: RecordViewMode = RecordViewMode.Projection,
	academicRecordRepository: ControllableAcademicRecordRepository = ControllableAcademicRecordRepository(
		initialRecord = record,
		initialHasSynced = true
	)
): RecordRouteFixture {
	val selectionRepository = RecordingRecordSelectionRepository(initialViewMode = viewMode)

	return RecordRouteFixture(
		viewModel = RecordViewModel(
			screenMachine = recordMachine(
				academicRecordRepository = academicRecordRepository,
				selectionRepository = selectionRepository
			),
			eventPublisher = NoOpEventPublisher
		),
		academicRecordRepository = academicRecordRepository,
		selectionRepository = selectionRepository
	)
}

fun scheduleRouteFixture(record: AcademicRecord, hasSynced: Boolean = true): ScheduleRouteFixture {
	val academicRecordRepository = ControllableAcademicRecordRepository(
		initialRecord = record,
		initialHasSynced = hasSynced
	)
	val selectionRepository = RecordingScheduleSelectionRepository()
	val reportingRepository = RecordingReportingRepository()

	return ScheduleRouteFixture(
		viewModel = ScheduleViewModel(
			screenMachine = ScheduleMachine(
				observeScheduleUseCase = ObserveScheduleUseCase(
					academicRecordRepository = academicRecordRepository,
					scheduleSelectionRepository = selectionRepository,
					scheduleClockRepository = ControllableScheduleClockRepository(),
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

fun createSyntheticTermRouteFixture(
	record: AcademicRecord = AcademicRecord(id = "record")
): CreateSyntheticTermRouteFixture {
	val academicRecordRepository = ControllableAcademicRecordRepository(
		initialRecord = record,
		initialHasSynced = true
	)
	val creationRepository = ControllableSyntheticTermCreationRepository()

	return CreateSyntheticTermRouteFixture(
		viewModel = CreateSyntheticTermViewModel(
			screenMachine = createSyntheticTermMachine(
				academicRecordRepository = academicRecordRepository,
				creationRepository = creationRepository
			),
			eventPublisher = NoOpEventPublisher
		),
		academicRecordRepository = academicRecordRepository,
		creationRepository = creationRepository
	)
}

private fun recordMachine(
	academicRecordRepository: ControllableAcademicRecordRepository,
	selectionRepository: RecordingRecordSelectionRepository
): RecordMachine {
	val syncStatusRepository = FakeSyncStatusRepository()
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
		)
	)
}

private fun createSyntheticTermMachine(
	academicRecordRepository: ControllableAcademicRecordRepository,
	creationRepository: ControllableSyntheticTermCreationRepository
): CreateSyntheticTermMachine {
	val reportingRepository = RecordingReportingRepository()
	val exceptionHandler = RecordExceptionHandler()

	return CreateSyntheticTermMachine(
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
			recordSelectionRepository = RecordingRecordSelectionRepository(),
			reportingRepository = reportingRepository
		)
	)
}
