package com.gdavidpb.tuindice.wizard.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.wizard.domain.usecase.CompleteWizardUseCase
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import com.gdavidpb.tuindice.wizard.presentation.viewmodel.WizardViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlin.test.Test
import kotlin.test.assertTrue

class WizardStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() {
		val machine = createViewModel().machine

		assertMachineCoversAlphabet(
			machine,
			Wizard.Action::class,
			WizardInternalEvent::class
		)

		assertMachineStatesReachable(
			machine = machine,
			initialState = Wizard.State.Content::class
		)

		assertMachineCoversEffects(machine, Wizard.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().exportMachineToMermaid()

		// Captured from test output to publish the generated diagram as a docs artifact.
		println(diagram)

		val expectedFragments = listOf(
			"content",
			"Advance",
			"Back",
			"ConsumeTopBarAction",
			"SetRecordViewMode",
			"SetSubjectChartsVisible",
			"WizardCompleted / FinishWizard"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	private fun createViewModel(): WizardViewModel {
		return WizardViewModel(
			screenMachine = WizardMachine(
				completeWizardUseCase = CompleteWizardUseCase(
					settingsRepository = FakeSettingsRepository(),
					reportingRepository = RecordingReportingRepository()
				)
			),
			eventPublisher = NoOpEventPublisher
		)
	}
}
