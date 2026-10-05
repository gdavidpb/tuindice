package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.di.recordModule
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationSnapshot
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import com.gdavidpb.tuindice.record.domain.repository.ScheduleClockRepository
import com.gdavidpb.tuindice.record.domain.repository.ScheduleSelectionRepository
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermCreationRepository
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermLoadPreviewRepository
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.presentation.model.CreateTermAddSubjectTab
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleGridItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleItem
import com.gdavidpb.tuindice.record.presentation.model.ScheduleTableItem
import com.gdavidpb.tuindice.record.presentation.viewmodel.CreateSyntheticTermViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.ScheduleViewModel
import com.gdavidpb.tuindice.record.testing.ControllableScheduleClockRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineHasNoShadowedRows
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.koin.core.Koin
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertTrue

// The machines need twelve use cases between them, so instead of hand-building that
// graph the tests boot the real Koin module with stubbed repositories and resolve the
// view models — which also smoke-tests the new machine registrations.
class RecordStateMachineContractTest {
	@Test
	fun recordMachine_coversAlphabet_andStatesAreReachable() = withMachineKoin {
		val machine = get<RecordViewModel>().machine

		assertMachineCoversAlphabet(
			machine,
			Record.Action::class,
			RecordInternalEvent::class
		)

		assertMachineHasNoShadowedRows(
			machine,
			Record.Action::class,
			RecordInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = Record.State.Idle::class
		)

		assertMachineCoversEffects(machine, Record.Effect::class)
	}

	@Test
	fun createSyntheticTermMachine_coversAlphabet_andStatesAreReachable() = withMachineKoin {
		val machine = get<CreateSyntheticTermViewModel>().machine

		assertMachineCoversAlphabet(
			machine,
			CreateSyntheticTerm.Action::class,
			CreateSyntheticTermInternalEvent::class
		)

		assertMachineHasNoShadowedRows(
			machine,
			CreateSyntheticTerm.Action::class,
			CreateSyntheticTermInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = CreateSyntheticTerm.State::class
		)

		assertMachineCoversEffects(machine, CreateSyntheticTerm.Effect::class)
	}

	@Test
	fun scheduleMachine_coversAlphabet_andStatesAreReachable() = withMachineKoin {
		val machine = get<ScheduleViewModel>().machine

		assertMachineCoversAlphabet(
			machine,
			Schedule.Action::class,
			ScheduleInternalEvent::class
		)

		assertMachineHasNoShadowedRows(
			machine,
			Schedule.Action::class,
			ScheduleInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = Schedule.State.Idle::class
		)

		assertMachineCoversEffects(machine, Schedule.Effect::class)
	}

	@Test
	fun machines_exportDeclaredTransitionsToMermaid() = withMachineKoin {
		val recordDiagram = get<RecordViewModel>()
			.machine.exportToMermaid(
				machineName = "record",
				initialState = Record.State.Idle::class
			)
		val createDiagram = get<CreateSyntheticTermViewModel>()
			.machine.exportToMermaid(
				machineName = "create_synthetic_term",
				initialState = CreateSyntheticTerm.State::class
			)

		// Captured from test output to publish the generated diagrams as docs artifacts.
		println(recordDiagram)
		println(createDiagram)

		val expectedRecordFragments = listOf(
			"idle",
			"loading",
			"content",
			"empty",
			"failed",
			"ObserveRecord",
			"EnsureRecordLoaded",
			"RecordContentObserved",
			"RecordWaitingObserved",
			"RecordRefreshFailed / NavigateToOutdatedCredentials",
			"RecordViewModeSet / ShowTopBarBanner",
			"SyntheticTermDeleted / ShowSnackBar",
			"SyntheticTermRejected / ShowSnackBar"
		)

		for (fragment in expectedRecordFragments) {
			assertTrue(
				recordDiagram.contains(fragment),
				"Expected record Mermaid export to mention '$fragment':\n$recordDiagram"
			)
		}

		val expectedCreateFragments = listOf(
			// The single state is named `State`; `state` is a Mermaid keyword, so it
			// renders via a safe aliased id with the readable name as its label.
			"state \"state\" as state_node",
			"Observe",
			"ConfigureTerm",
			"UpdateQuery",
			"SelectAddSubjectTab",
			"SelectPeriod",
			"AddSubject",
			"RemoveSubject",
			"SnapshotObserved",
			"SearchStarted",
			"LoadPreviewLoaded",
			"SubmitSucceeded / NavigateBack"
		)

		for (fragment in expectedCreateFragments) {
			assertTrue(
				createDiagram.contains(fragment),
				"Expected create-term Mermaid export to mention '$fragment':\n$createDiagram"
			)
		}
	}

	// Its own test so the record export above stays readable; the name still ends in ToMermaid,
	// which is what scripts/dump-machine-diagrams.sh filters on.
	@Test
	fun scheduleMachine_exportsDeclaredTransitionsToMermaid() = withMachineKoin {
		val scheduleDiagram = get<ScheduleViewModel>()
			.machine.exportToMermaid(
				machineName = "schedule",
				initialState = Schedule.State.Idle::class
			)

		// Captured from test output to publish the generated diagram as a docs artifact.
		println(scheduleDiagram)

		val expectedScheduleFragments = listOf(
			"idle",
			"loading",
			"content",
			"empty",
			"ObserveSchedule",
			"SelectScheduleView",
			"ScheduleContentObserved",
			"ScheduleEmptyObserved",
			"ScheduleWaitingObserved"
		)

		for (fragment in expectedScheduleFragments) {
			assertTrue(
				scheduleDiagram.contains(fragment),
				"Expected schedule Mermaid export to mention '$fragment':\n$scheduleDiagram"
			)
		}
	}

	@Test
	fun recordMachine_survivesSeededRandomWalk() = runTest {
		// The walk is suspend and withMachineKoin's block is not, so the machine is
		// resolved inside the Koin scope and walked after it closes: the machine is a
		// plain constructor-injected object graph over the stub repositories.
		var resolvedMachine: RecordMachine? = null

		withMachineKoin {
			resolvedMachine = get()
		}

		assertMachineRandomWalk(
			screenMachine = requireNotNull(resolvedMachine),
			sampleEvents = listOf(
				Record.Action.ObserveRecord,
				Record.Action.EnsureRecordLoaded,
				Record.Action.RefreshRecord,
				Record.Action.SetViewMode(viewMode = RecordViewMode.Historical),
				Record.Action.SelectTerm(termId = "term-1"),
				Record.Action.UpsertAttemptSelection(
					attemptId = "attempt-1",
					grade = 15,
					commit = true
				),
				Record.Action.DeleteSyntheticTerm(termId = "term-synthetic-1"),
				RecordInternalEvent.RecordContentObserved(
					viewMode = RecordViewMode.Projection,
					record = AcademicRecord(id = "record-1"),
					selectedTermId = "term-1",
					notice = null
				),
				RecordInternalEvent.RecordEmptyObserved(notice = null),
				RecordInternalEvent.RecordWaitingObserved(isNewStudentNoRecord = false),
				RecordInternalEvent.NewStudentNoRecordObserved(isNewStudentNoRecord = true),
				RecordInternalEvent.RecordObservationFailed,
				RecordInternalEvent.RecordRefreshStarted,
				RecordInternalEvent.RecordRefreshFailed(
					message = "Comprueba tu conexión",
					navigateToOutdatedCredentials = false,
					isNewStudentNoRecord = false
				),
				RecordInternalEvent.RecordViewModeSet(viewMode = RecordViewMode.Projection),
				RecordInternalEvent.RecordUnauthorized,
				RecordInternalEvent.AttemptSelectionFailed(message = "Comprueba tu conexión"),
				RecordInternalEvent.SyntheticTermDeleted(message = "Período eliminado"),
				RecordInternalEvent.SyntheticTermDeleteFailed(
					message = "No se pudo eliminar",
					navigateToOutdatedCredentials = false
				),
				RecordInternalEvent.SyntheticTermRejected(message = "Cambio rechazado")
			),
			coroutineScope = backgroundScope,
			// Conservative floor: every internal event is sampled by hand; raise to the
			// observed coverage once the walk has run on CI.
			minRowCoverage = 0.4
		)
	}

	@Test
	fun createSyntheticTermMachine_survivesSeededRandomWalk() = runTest {
		var resolvedMachine: CreateSyntheticTermMachine? = null

		withMachineKoin {
			resolvedMachine = get()
		}

		val period = SyntheticTermPeriodOption(
			periodYear = 2026,
			periodCode = AcademicTermPeriod.JAN_MAR
		)
		val subjectItem = CreateTermSubjectItem(
			subject = SyntheticTermSubject(
				subjectCode = "CI2125",
				name = "Algoritmos y Estructuras I",
				credits = 4
			),
			nameText = "Algoritmos y Estructuras I"
		)

		assertMachineRandomWalk(
			screenMachine = requireNotNull(resolvedMachine),
			sampleEvents = listOf(
				CreateSyntheticTerm.Action.Observe,
				CreateSyntheticTerm.Action.ConfigureTerm(termId = null),
				CreateSyntheticTerm.Action.UpdateQuery(
					query = "algoritmos",
					selectionStart = 10,
					selectionEnd = 10
				),
				CreateSyntheticTerm.Action.SelectAddSubjectTab(
					tab = CreateTermAddSubjectTab.Search
				),
				CreateSyntheticTerm.Action.SelectPeriod(termKey = period.termKey),
				CreateSyntheticTerm.Action.AddSubject(subjectItem = subjectItem),
				CreateSyntheticTerm.Action.RemoveSubject(subjectCode = "CI2125"),
				CreateSyntheticTerm.Action.CreateTerm,
				CreateSyntheticTermInternalEvent.SnapshotObserved(
					editingTermId = null,
					editingTermKey = null,
					periodOptions = listOf(period),
					selectedPeriod = period,
					selectedSubjects = listOf(subjectItem),
					suggestedSubjects = emptyList(),
					searchResults = emptyList()
				),
				CreateSyntheticTermInternalEvent.SearchCleared,
				CreateSyntheticTermInternalEvent.SearchStarted,
				CreateSyntheticTermInternalEvent.SearchSucceeded,
				CreateSyntheticTermInternalEvent.SearchFailed,
				CreateSyntheticTermInternalEvent.LoadPreviewCleared,
				CreateSyntheticTermInternalEvent.LoadPreviewStarted,
				CreateSyntheticTermInternalEvent.LoadPreviewLoaded(
					preview = SyntheticTermLoadPreview(available = false)
				),
				CreateSyntheticTermInternalEvent.LoadPreviewFailed,
				CreateSyntheticTermInternalEvent.SubmitStarted,
				CreateSyntheticTermInternalEvent.SubmitSucceeded,
				CreateSyntheticTermInternalEvent.SubmitFailed(error = UiText.Empty)
			),
			coroutineScope = backgroundScope,
			// Conservative floor: single state class, so every row resolves from these
			// samples; raise to the observed coverage once the walk has run on CI.
			minRowCoverage = 0.5
		)
	}

	@Test
	fun scheduleMachine_survivesSeededRandomWalk() = runTest {
		var resolvedMachine: ScheduleMachine? = null

		withMachineKoin {
			resolvedMachine = get()
		}

		assertMachineRandomWalk(
			screenMachine = requireNotNull(resolvedMachine),
			sampleEvents = listOf(
				Schedule.Action.ObserveSchedule,
				Schedule.Action.SelectScheduleView(viewMode = ScheduleViewMode.Week),
				ScheduleInternalEvent.ScheduleContentObserved(
					termName = "SEP-DIC 2026",
					schedule = ScheduleItem(
						grid = ScheduleGridItem(blockCount = 1, days = emptyList(), unscheduledText = null),
						table = ScheduleTableItem(days = emptyList(), rows = emptyList())
					),
					viewMode = ScheduleViewMode.Table
				),
				ScheduleInternalEvent.ScheduleEmptyObserved,
				ScheduleInternalEvent.ScheduleWaitingObserved
			),
			coroutineScope = backgroundScope,
			// Five samples over a handful of rows: every row resolves from them.
			minRowCoverage = 0.5
		)
	}

	private fun withMachineKoin(block: Koin.() -> Unit) = withKoinSmokeTest(
		recordModule,
		module {
			single<AcademicRecordRepository> { StubAcademicRecordRepository() }
			single<RecordSelectionRepository> { StubRecordSelectionRepository() }
			single<ScheduleSelectionRepository> { StubScheduleSelectionRepository() }
			// Overrides the module's clock: the real one ticks forever, and the walks wait for idle.
			single<ScheduleClockRepository> { ControllableScheduleClockRepository() }
			single<SyntheticTermCreationRepository> { StubSyntheticTermCreationRepository() }
			single<SyntheticTermLoadPreviewRepository> { StubSyntheticTermLoadPreviewRepository() }
			single<ReportingRepository> { RecordingReportingRepository() }
			single<SyncStatusRepository> { FakeSyncStatusRepository() }
			single<EventPublisher> { NoOpEventPublisher }
			single<TuIndiceDispatchers> { DefaultTuIndiceDispatchers }
		},
		block = block
	)
}

private class StubAcademicRecordRepository : AcademicRecordRepository {
	override suspend fun observeAcademicRecordFlow(): Flow<AcademicRecord> = emptyFlow()

	override suspend fun observeHasSyncedRecordFlow(): Flow<Boolean> = flowOf(false)

	override suspend fun getAcademicRecord(): AcademicRecord? = null

	override suspend fun updateAcademicRecord() = Unit

	override suspend fun drainPendingMutations() = Unit

	override suspend fun upsertAttemptOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?
	) = Unit

	override suspend fun deleteAttemptOverride(attemptId: String) = Unit

	override suspend fun addSyntheticTerm(command: SyntheticTermCreationCommand) = Unit

	override suspend fun updateSyntheticTerm(command: SyntheticTermUpdateCommand) = Unit

	override suspend fun deleteSyntheticTerm(termId: String) = Unit
}

private class StubRecordSelectionRepository : RecordSelectionRepository {
	override fun observeSelectedTermId(viewMode: RecordViewMode): Flow<String?> = flowOf(null)

	override fun observeRecordViewMode(): Flow<RecordViewMode> = flowOf(RecordViewMode.Projection)

	override suspend fun getSelectedTermId(viewMode: RecordViewMode): String? = null

	override suspend fun setSelectedTermId(viewMode: RecordViewMode, termId: String) = Unit

	override suspend fun getRecordViewMode(): RecordViewMode = RecordViewMode.Projection

	override suspend fun setRecordViewMode(viewMode: RecordViewMode) = Unit
}

private class StubScheduleSelectionRepository : ScheduleSelectionRepository {
	override fun observeScheduleViewMode(): Flow<ScheduleViewMode> = flowOf(ScheduleViewMode.Table)

	override suspend fun setScheduleViewMode(viewMode: ScheduleViewMode) = Unit
}

private class StubSyntheticTermCreationRepository : SyntheticTermCreationRepository {
	override fun observeSnapshot(
		queryFlow: StateFlow<String>,
		selectedSubjectsFlow: StateFlow<List<SyntheticTermSubject>>,
		selectedPeriodKeyFlow: StateFlow<String?>,
		editingTermIdFlow: StateFlow<String?>,
		editingTermKeyFlow: StateFlow<String?>
	): Flow<SyntheticTermCreationSnapshot> = emptyFlow()

	override suspend fun refreshSearch(query: String) = Unit
}

private class StubSyntheticTermLoadPreviewRepository : SyntheticTermLoadPreviewRepository {
	override suspend fun loadSyntheticTermPreview(
		termKey: String,
		subjectCodes: List<String>
	): SyntheticTermLoadPreview = error("Not exercised by machine contract tests")
}
