package com.gdavidpb.tuindice.pensum.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.pensum.domain.model.ObservedPensum
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumModality
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.model.PensumOption
import com.gdavidpb.tuindice.pensum.domain.model.PensumSelection
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.pensum.domain.usecase.EnsurePensumLoadedUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.ObservePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumModalityUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumSelectionUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.UpdatePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.viewmodel.PensumViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class PensumStateMachineContractTest {
	@Test
	fun machine_coversTheFullInputAlphabet() {
		assertMachineCoversAlphabet(
			createViewModel().machine,
			Pensum.Action::class,
			PensumInternalEvent::class
		)
	}

	@Test
	fun machine_declaresTheFullOutputAlphabet() {
		assertMachineCoversEffects(
			createViewModel().machine,
			Pensum.Effect::class
		)
	}

	@Test
	fun machine_statesAreReachableFromIdle() {
		assertMachineStatesReachable(
			machine = createViewModel().machine,
			initialState = Pensum.State.Idle::class
		)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().machine.exportToMermaid(
			machineName = "pensum",
			initialState = Pensum.State.Idle::class
		)

		// Captured from test output to publish the generated diagram as a docs artifact.
		println(diagram)

		val expectedFragments = listOf(
			"idle",
			"loading",
			"content",
			"empty",
			"record_data_unavailable",
			"failed",
			"ObservePensum",
			"PensumContentObserved",
			"PensumRefreshNotFound",
			"SelectSelection"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		val repository = StaticPensumRepository()
		val reportingRepository = RecordingReportingRepository()
		val exceptionHandler = UpdatePensumExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		)

		val screenMachine = PensumMachine(
			observePensumUseCase = ObservePensumUseCase(
				pensumRepository = repository,
				reportingRepository = reportingRepository
			),
			ensurePensumLoadedUseCase = EnsurePensumLoadedUseCase(
				pensumRepository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			updatePensumUseCase = UpdatePensumUseCase(
				pensumRepository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			selectPensumUseCase = SelectPensumUseCase(
				pensumRepository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			selectPensumModalityUseCase = SelectPensumModalityUseCase(
				pensumRepository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			),
			selectPensumSelectionUseCase = SelectPensumSelectionUseCase(
				pensumRepository = repository,
				reportingRepository = reportingRepository,
				exceptionHandler = exceptionHandler
			)
		)

		val graph = PensumGraph(
			id = "pensum-1970",
			year = 1970,
			modalityId = "modality-diurna",
			modalityName = "Diurna",
			totalCredits = 180,
			canvas = PensumGraph.Canvas(width = 0.0, height = 0.0),
			terms = emptyList(),
			nodes = emptyList(),
			edges = emptyList()
		)
		val observedPensum = ObservedPensum(
			careerName = "Ingeniería de Computación",
			selection = PensumSelection(
				pensumId = "pensum-1970",
				year = 1970,
				modalityId = "modality-diurna",
				modalityName = "Diurna",
				inferred = false
			),
			availablePensums = listOf(PensumOption(id = "pensum-1970", year = 1970)),
			availableModalities = listOf(
				PensumModality(id = "modality-diurna", name = "Diurna", isDefault = true)
			),
			pensum = graph,
			pensums = listOf(graph),
			approvedCredits = 42,
			nodeStatuses = emptyMap(),
			nodeFulfillments = emptyMap()
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				Pensum.Action.ObservePensum,
				Pensum.Action.EnsurePensumLoaded,
				Pensum.Action.RefreshPensum,
				Pensum.Action.SelectPensum(year = 1970),
				Pensum.Action.SelectModality(modalityId = "modality-diurna"),
				Pensum.Action.SelectSelection(year = 1970, modalityId = "modality-diurna"),
				PensumInternalEvent.PensumContentObserved(pensum = observedPensum),
				PensumInternalEvent.PensumDataMissing,
				PensumInternalEvent.PensumRecordDataUnavailableObserved,
				PensumInternalEvent.PensumObservationFailed,
				PensumInternalEvent.PensumRefreshLoading,
				PensumInternalEvent.PensumRefreshSucceeded,
				PensumInternalEvent.PensumRefreshNotFound,
				PensumInternalEvent.PensumRefreshFailed(error = null)
			),
			scope = backgroundScope,
			// Conservative floor: every internal event is sampled by hand; raise to the
			// observed coverage once the walk has run on CI.
			minRowCoverage = 0.4
		)
	}

	private fun createViewModel(): PensumViewModel {
		val repository = StaticPensumRepository()
		val reportingRepository = RecordingReportingRepository()
		val exceptionHandler = UpdatePensumExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		)

		return PensumViewModel(
			screenMachine = PensumMachine(
				observePensumUseCase = ObservePensumUseCase(
					pensumRepository = repository,
					reportingRepository = reportingRepository
				),
				ensurePensumLoadedUseCase = EnsurePensumLoadedUseCase(
					pensumRepository = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				updatePensumUseCase = UpdatePensumUseCase(
					pensumRepository = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				selectPensumUseCase = SelectPensumUseCase(
					pensumRepository = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				selectPensumModalityUseCase = SelectPensumModalityUseCase(
					pensumRepository = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				),
				selectPensumSelectionUseCase = SelectPensumSelectionUseCase(
					pensumRepository = repository,
					reportingRepository = reportingRepository,
					exceptionHandler = exceptionHandler
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}

private class StaticPensumRepository : PensumRepository {
	override fun observePensumFlow(): Flow<PensumObservation> = emptyFlow()

	override suspend fun hasSelectedPensumResponse(): Boolean = false

	override suspend fun refreshPensum() = Unit

	override suspend fun selectPensum(year: Int) = Unit

	override suspend fun selectModality(modalityId: String) = Unit

	override suspend fun selectSelection(year: Int, modalityId: String) = Unit
}
