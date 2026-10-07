package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The password toggle scenario proves what the field shows once the password is visible, not that it survives. */
class PasswordToggleScenarioTest {
	private val steps = E2eCatalog.all.single { it.id == "auth-login-password-toggle" }.steps

	@Test
	fun theFirstToggleIsFollowedByTheTypedPasswordShownInTheField() {
		val toggle = steps.indexOfFirst { it is Step.Tap && it.q == Query.Tag(AuthUiTags.PasswordToggle) }

		assertTrue(toggle >= 0, "the scenario never taps the toggle")
		assertTrue(
			steps.drop(toggle + 1).take(AFTER_TOGGLE).any {
				it is Step.WaitVisible && it.q == Query.Text(E2eAccounts.Canonical.password)
			},
			"the scenario does not wait for the typed password to show after the first toggle"
		)
	}

	@Test
	fun theSecondToggleComesAfterThatAssertion() {
		val toggles = steps.withIndex().filter { (_, step) ->
			step is Step.Tap && step.q == Query.Tag(AuthUiTags.PasswordToggle)
		}
		val shown = steps.indexOfFirst { it is Step.WaitVisible && it.q == Query.Text(E2eAccounts.Canonical.password) }

		assertEquals(2, toggles.size)
		assertTrue(shown in (toggles.first().index + 1) until toggles.last().index)
	}

	private companion object {
		const val AFTER_TOGGLE = 2
	}
}
