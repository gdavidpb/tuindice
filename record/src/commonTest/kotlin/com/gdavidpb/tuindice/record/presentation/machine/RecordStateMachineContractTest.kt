package com.gdavidpb.tuindice.record.presentation.machine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.record.di.recordModule
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationSnapshot
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermCreationRepository
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermLoadPreviewRepository
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.viewmodel.CreateSyntheticTermViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.koin.withKoinSmokeTest
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.Koin
import org.koin.dsl.module

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

		assertMachineStatesReachable(
			machine = machine,
			initialState = CreateSyntheticTerm.State::class
		)

		// ShowSnackBar is collected by CreateSyntheticTermRoute but no transition emits
		// it today — a dead Λ symbol surfaced by the validator, pending a decision at
		// ratification (remove from the contract or wire its emitter).
		assertMachineCoversEffects(
			machine,
			CreateSyntheticTerm.Effect::class,
			except = setOf(CreateSyntheticTerm.Effect.ShowSnackBar::class)
		)
	}

	@Test
	fun machines_exportDeclaredTransitionsToMermaid() = withMachineKoin {
		val recordDiagram = get<RecordViewModel>()
			.exportMachineToMermaid()
		val createDiagram = get<CreateSyntheticTermViewModel>()
			.exportMachineToMermaid()

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
			"state state",
			"Observe",
			"UpdateQuery",
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
