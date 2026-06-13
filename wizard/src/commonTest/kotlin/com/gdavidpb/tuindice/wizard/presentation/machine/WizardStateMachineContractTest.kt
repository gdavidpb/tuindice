package com.gdavidpb.tuindice.wizard.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.wizard.domain.usecase.CompleteWizardUseCase
import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import com.gdavidpb.tuindice.wizard.presentation.viewmodel.WizardViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

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

	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		val screenMachine = WizardMachine(
			completeWizardUseCase = CompleteWizardUseCase(
				settingsRepository = FakeSettingsRepository(),
				reportingRepository = RecordingReportingRepository()
			)
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				Wizard.Action.Advance,
				Wizard.Action.Back,
				Wizard.Action.Dismiss,
				Wizard.Action.Finish,
				Wizard.Action.OpenSubjectDetail,
				Wizard.Action.OpenEvaluationForm,
				Wizard.Action.ConsumeTopBarAction(topBarAction = TopBarAction.RecordTermSelectionAction),
				Wizard.Action.SelectSubjectTab(tab = SubjectSegmentTab.GLOBAL),
				Wizard.Action.SetRecordViewMode(viewMode = RecordViewMode.Historical),
				Wizard.Action.SelectTerm(termId = "term-2026-1"),
				Wizard.Action.DismissRecordTermSelection,
				Wizard.Action.SetSubjectChartsVisible(isVisible = true),
				WizardInternalEvent.WizardCompleted
			),
			scope = backgroundScope,
			// Conservative floor: single state class, so every row resolves from these
			// samples; raise to the observed coverage once the walk has run on CI.
			minRowCoverage = 0.5
		)
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
