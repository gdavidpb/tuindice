package com.gdavidpb.tuindice.subjects.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.subjects.domain.usecase.LoadSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.ObserveSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectDetailUseCase
import com.gdavidpb.tuindice.subjects.domain.usecase.RefreshSubjectSearchUseCase
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectSearch
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectDetailViewModel
import com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectSearchViewModel
import com.gdavidpb.tuindice.subjects.testing.ControllableSubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.testing.RecordingSubjectStatsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import kotlin.test.Test
import kotlin.test.assertTrue

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
		val detailDiagram = createDetailViewModel().exportMachineToMermaid()
		val searchDiagram = createSearchViewModel().exportMachineToMermaid()

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
			"state state",
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
