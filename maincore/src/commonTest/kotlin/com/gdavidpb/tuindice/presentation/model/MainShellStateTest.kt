package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.wizard.presentation.contract.Wizard
import kotlin.test.Test
import kotlin.test.assertFalse

class MainShellStateTest {
	@Test
	fun when_viewStateIsWizard_then_topBarBackButtonIsHidden() {
		val shellState = Wizard.State.Content(currentIndex = 1).toMainShellState()

		assertFalse(shellState.showsTopBarBackButton)
	}
}
