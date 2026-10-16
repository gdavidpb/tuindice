package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.summary.presentation.contract.ShareTerm
import com.gdavidpb.tuindice.summary.presentation.viewmodel.ShareTermViewModel
import com.gdavidpb.tuindice.summary.testing.shareTermMachineOf
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversAlphabet
import com.gdavidpb.tuindice.testkit.mvi.assertMachineCoversEffects
import com.gdavidpb.tuindice.testkit.mvi.assertMachineHasNoShadowedRows
import com.gdavidpb.tuindice.testkit.mvi.assertMachineStatesReachable
import com.gdavidpb.tuindice.testkit.mvi.exportToMermaid
import kotlin.test.Test
import kotlin.test.assertTrue

class ShareTermStateMachineContractTest {
	@Test
	fun machine_coversAlphabet_andStatesAreReachable() {
		val machine = createViewModel().machine

		assertMachineCoversAlphabet(machine, ShareTerm.Action::class, ShareTermInternalEvent::class)

		assertMachineHasNoShadowedRows(machine, ShareTerm.Action::class, ShareTermInternalEvent::class)

		assertMachineStatesReachable(
			machine = machine,
			initialState = ShareTerm.State.Idle::class
		)

		assertMachineCoversEffects(machine, ShareTerm.Effect::class)
	}

	@Test
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = createViewModel().machine.exportToMermaid(
			machineName = "summary_share_term",
			initialState = ShareTerm.State.Idle::class
		)

		val expectedFragments = listOf(
			"idle",
			"loading",
			"content",
			"unavailable",
			"ObserveShareTerm",
			"TermsObserved",
			"TermsUnavailable",
			"SelectTerm",
			"SelectBackground",
			"ShareImage",
			"ImageShared",
			"ImageShareFailed"
		)

		for (fragment in expectedFragments) {
			assertTrue(
				diagram.contains(fragment),
				"Expected Mermaid export to mention '$fragment':\n$diagram"
			)
		}
	}

	private fun createViewModel(): ShareTermViewModel {
		return ShareTermViewModel(
			screenMachine = shareTermMachineOf(),
			eventPublisher = NoOpEventPublisher
		)
	}
}
