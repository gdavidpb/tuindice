package com.gdavidpb.tuindice.subjects.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.domain.usecase.LoadSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.ObserveSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectDetailViewModel
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectSearchViewModel
import com.gdavidpb.tuindice.subjects.testing.ControllableSubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.testing.RecordingSubjectStatsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SubjectsStateMachineContractTest {
	@Test
	fun detailMachine_coversAlphabet_andStatesAreReachable() {
		val machine = createDetailViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			SubjectDetail.Action::class,
			SubjectDetailInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = SubjectDetail.State.Idle::class
		)

		assertMachineCoversEffects(machine, SubjectDetail.Effect::class)
	}

	@Test
	fun searchMachine_coversAlphabet_andStatesAreReachable() {
		val machine = createSearchViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			SubjectSearch.Action::class,
			SubjectSearchInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = SubjectSearch.State::class
		)

		assertMachineCoversEffects(machine, SubjectSearch.Effect::class)
	}

	@Test
	fun machines_exportDeclaredTransitionsToMermaid() {
		val detailDiagram = createDetailViewModel().machine.exportToMermaid(
			machineName = "subject_detail",
			initialState = SubjectDetail.State.Idle::class
		)
		val searchDiagram = createSearchViewModel().machine.exportToMermaid(
			machineName = "subject_search",
			initialState = SubjectSearch.State::class
		)

		// Captured from test output to publish the generated diagrams as docs artifacts.
		println(detailDiagram)
		println(searchDiagram)

		val expectedDetailFragments = listOf(
			"idle",
			"loading",
			"content",
			"unavailable",
			"failed",
			"LoadSubjectDetail",
			"DetailContentLoaded",
			"DetailRefreshFailed",
			"SelectSubjectSegmentTab"
		)

		for (fragment in expectedDetailFragments) {
			assertTrue(
				detailDiagram.contains(fragment),
				"Expected detail Mermaid export to mention '$fragment':\n$detailDiagram"
			)
		}

		val expectedSearchFragments = listOf(
			// The single state is named `State`; `state` is a Mermaid keyword, so it
			// renders via a safe aliased id with the readable name as its label.
			"state \"state\" as state_node",
			"UpdateQuery",
			"LocalResultsChanged",
			"RemoteSearchStarted",
			"RetryCleared"
		)

		for (fragment in expectedSearchFragments) {
			assertTrue(
				searchDiagram.contains(fragment),
				"Expected search Mermaid export to mention '$fragment':\n$searchDiagram"
			)
		}
	}

	@Test
	fun detailMachine_survivesSeededRandomWalk() = runTest {
		val repository = RecordingSubjectStatsRepository()
		val reportingRepository = RecordingReportingRepository()

		val screenMachine = SubjectDetailMachine(
			loadSubjectDetailUseCase = LoadSubjectDetailUseCase(
				subjectStatsRepository = repository,
				reportingRepository = reportingRepository
			),
			refreshSubjectDetailUseCase = RefreshSubjectDetailUseCase(
				subjectStatsRepository = repository,
				reportingRepository = reportingRepository
			)
		)

		val detail = SubjectDetailItem(
			id = "CI2125",
			name = "Algoritmos y Estructuras I",
			creditsText = "4 UC",
			gradingModeText = null,
			generatedAtText = "Generado el 12/06/2026",
			selectedTab = SubjectSegmentTab.CAREER,
			hasSegmentTabs = false,
			chartMode = SubjectDetailItem.ChartMode.NUMERIC_GRADES,
			careerSegment = null,
			globalSegment = null
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				SubjectDetail.Action.LoadSubjectDetail(subjectCode = "CI2125"),
				SubjectDetail.Action.RefreshSubjectDetail(subjectCode = "CI2125"),
				SubjectDetail.Action.SelectSubjectSegmentTab(tab = SubjectSegmentTab.GLOBAL),
				SubjectDetailInternalEvent.DetailLoadStarted,
				SubjectDetailInternalEvent.DetailRefreshStarted,
				SubjectDetailInternalEvent.DetailContentLoaded(detail = detail),
				SubjectDetailInternalEvent.DetailUnavailableLoaded(subjectCode = "CI2125"),
				SubjectDetailInternalEvent.DetailLoadFailed(subjectCode = "CI2125"),
				SubjectDetailInternalEvent.DetailRefreshFailed(subjectCode = "CI2125")
			),
			scope = backgroundScope,
			// Conservative floor: every internal event is sampled by hand; raise to the
			// observed coverage once the walk has run on CI.
			minRowCoverage = 0.4
		)
	}

	@Test
	fun searchMachine_survivesSeededRandomWalk() = runTest {
		val repository = ControllableSubjectCatalogRepository()
		val reportingRepository = RecordingReportingRepository()

		val screenMachine = SubjectSearchMachine(
			draft = SubjectSearchDraft(),
			observeSubjectSearchUseCase = ObserveSubjectSearchUseCase(
				subjectCatalogRepository = repository,
				reportingRepository = reportingRepository
			),
			refreshSubjectSearchUseCase = RefreshSubjectSearchUseCase(
				subjectCatalogRepository = repository,
				reportingRepository = reportingRepository
			)
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				SubjectSearch.Action.ObserveSubjectSearch,
				SubjectSearch.Action.UpdateQuery(query = "algoritmos"),
				SubjectSearch.Action.Retry,
				SubjectSearchInternalEvent.ShortQueryCleared(query = "al"),
				SubjectSearchInternalEvent.LocalResultsChanged(
					query = "algoritmos",
					results = emptyList()
				),
				SubjectSearchInternalEvent.RemoteSearchStarted(query = "algoritmos"),
				SubjectSearchInternalEvent.RemoteSearchSucceeded,
				SubjectSearchInternalEvent.RemoteSearchFailed,
				SubjectSearchInternalEvent.RetryStarted,
				SubjectSearchInternalEvent.RetryCleared
			),
			scope = backgroundScope,
			// Conservative floor: single state class, so every row resolves from these
			// samples; raise to the observed coverage once the walk has run on CI.
			minRowCoverage = 0.5
		)
	}

	private fun createDetailViewModel(): SubjectDetailViewModel {
		val repository = RecordingSubjectStatsRepository()
		val reportingRepository = RecordingReportingRepository()

		return SubjectDetailViewModel(
			screenMachine = SubjectDetailMachine(
				loadSubjectDetailUseCase = LoadSubjectDetailUseCase(
					subjectStatsRepository = repository,
					reportingRepository = reportingRepository
				),
				refreshSubjectDetailUseCase = RefreshSubjectDetailUseCase(
					subjectStatsRepository = repository,
					reportingRepository = reportingRepository
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}

	private fun createSearchViewModel(): SubjectSearchViewModel {
		val repository = ControllableSubjectCatalogRepository()
		val reportingRepository = RecordingReportingRepository()

		return SubjectSearchViewModel(
			screenMachine = SubjectSearchMachine(
				draft = SubjectSearchDraft(),
				observeSubjectSearchUseCase = ObserveSubjectSearchUseCase(
					subjectCatalogRepository = repository,
					reportingRepository = reportingRepository
				),
				refreshSubjectSearchUseCase = RefreshSubjectSearchUseCase(
					subjectCatalogRepository = repository,
					reportingRepository = reportingRepository
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
