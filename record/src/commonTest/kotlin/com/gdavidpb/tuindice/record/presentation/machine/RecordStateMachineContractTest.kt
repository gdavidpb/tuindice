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
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.di.recordModule
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationSnapshot
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermCreationRepository
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermLoadPreviewRepository
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.model.CreateTermAddSubjectTab
import com.gdavidpb.tuindice.record.presentation.model.CreateTermSubjectItem
import com.gdavidpb.tuindice.record.presentation.viewmodel.CreateSyntheticTermViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
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
			"SyntheticTermDeleted / ShowSnackBar"
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
					selectedTermId = "term-1"
				),
				RecordInternalEvent.RecordEmptyObserved,
				RecordInternalEvent.RecordWaitingObserved,
				RecordInternalEvent.RecordObservationFailed,
				RecordInternalEvent.RecordRefreshStarted,
				RecordInternalEvent.RecordRefreshFailed(
					message = "Comprueba tu conexión",
					navigateToOutdatedCredentials = false
				),
				RecordInternalEvent.RecordViewModeSet(viewMode = RecordViewMode.Projection),
				RecordInternalEvent.RecordUnauthorized,
				RecordInternalEvent.AttemptSelectionFailed(message = "Comprueba tu conexión"),
				RecordInternalEvent.SyntheticTermDeleted(message = "Período eliminado"),
				RecordInternalEvent.SyntheticTermDeleteFailed(
					message = "No se pudo eliminar",
					navigateToOutdatedCredentials = false
				)
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

	private fun withMachineKoin(block: Koin.() -> Unit) = withKoinSmokeTest(
		recordModule,
		module {
			single<AcademicRecordRepository> { StubAcademicRecordRepository() }
			single<RecordSelectionRepository> { StubRecordSelectionRepository() }
			single<SyntheticTermCreationRepository> { StubSyntheticTermCreationRepository() }
			single<SyntheticTermLoadPreviewRepository> { StubSyntheticTermLoadPreviewRepository() }
			single<ReportingRepository> { RecordingReportingRepository() }
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
		outcome: AttemptOutcome?,
		commit: Boolean
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
