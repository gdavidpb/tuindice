package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryStatisticsViewModel
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineHasNoShadowedRows
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SummaryStatisticsStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() {
		val machine = createViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			SummaryStatistics.Action::class,
			SummaryStatisticsInternalEvent::class
		)

		assertMachineHasNoShadowedRows(
			machine,
			SummaryStatistics.Action::class,
			SummaryStatisticsInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = SummaryStatistics.State.Idle::class
		)

		assertMachineCoversEffects(machine, SummaryStatistics.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().machine.exportToMermaid(
			machineName = "summary_statistics",
			initialState = SummaryStatistics.State.Idle::class
		)

		// Captured from test output to publish the generated diagram as a docs artifact.
		println(diagram)

		val expectedFragments = listOf(
			"idle",
			"loading",
			"content",
			"unavailable",
			"ObserveStatistics",
			"StatisticsObserved",
			"StatisticsUnavailable",
			"Resumed",
			"OpenTarget / NavigateTo"
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
		assertMachineRandomWalk(
			screenMachine = createMachine(),
			sampleEvents = listOf(
				SummaryStatistics.Action.ObserveStatistics,
				SummaryStatistics.Action.Resumed,
				SummaryStatisticsInternalEvent.StatisticsUnavailable
			),
			coroutineScope = backgroundScope
		)
	}

	private fun createMachine(): SummaryStatisticsMachine {
		return SummaryStatisticsMachine(
			observeSummaryHubUseCase = createObserveSummaryHubUseCase()
		)
	}

	private fun createViewModel(): SummaryStatisticsViewModel {
		return SummaryStatisticsViewModel(
			screenMachine = createMachine(),
			eventPublisher = NoOpEventPublisher
		)
	}
}
