package com.gdavidpb.tuindice.pensum.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
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
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

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
		val diagram = createViewModel().exportMachineToMermaid()

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

	private fun createViewModel(): PensumViewModel {
		val repository = StaticPensumRepository()
		val reportingRepository = RecordingReportingRepository()
		val exceptionHandler = UpdatePensumExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		)

		return PensumViewModel(
			observePensumUseCase = ObservePensumUseCase(
				pensumRepository = repository,
				reportingRepository = reportingRepository
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
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}

private class StaticPensumRepository : PensumRepository {
	override fun observePensumFlow(): Flow<PensumObservation> = emptyFlow()

	override suspend fun refreshPensum() = Unit

	override suspend fun selectPensum(year: Int) = Unit

	override suspend fun selectModality(modalityId: String) = Unit

	override suspend fun selectSelection(year: Int, modalityId: String) = Unit
}
