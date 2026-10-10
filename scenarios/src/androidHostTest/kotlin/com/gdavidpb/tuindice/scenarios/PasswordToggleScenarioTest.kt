package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The scenarios that tap the eye of a password field prove what the field shows once the password is visible, not
 * that the tap survives: after the first toggle they wait for the typed password as text, and the second toggle
 * comes after that.
 */
class PasswordToggleScenarioTest {
	/** Scenario id and the password it typed. */
	private val typed = mapOf(
		"auth-login-password-toggle" to E2eAccounts.Canonical.password,
		"auth-update-password" to E2eFixtures.UpdatedPassword
	)

	private fun stepsOf(id: String) = E2eCatalog.all.single { it.id == id }.steps.flattened()

	@Test
	fun theFirstToggleIsFollowedByTheTypedPasswordShownInTheField() {
		typed.forEach { (id, password) ->
			val steps = stepsOf(id)
			val toggle = steps.indexOfFirst { it is Step.Tap && it.q == Query.Tag(AuthUiTags.PasswordToggle) }

			assertTrue(toggle >= 0, "$id never taps the toggle")
			assertTrue(
				steps.drop(toggle + 1).take(AFTER_TOGGLE).any { it is Step.WaitVisible && it.q == Query.Text(password) },
				"$id does not wait for the typed password to show after the first toggle"
			)
		}
	}

	@Test
	fun theSecondToggleComesAfterThatAssertion() {
		typed.forEach { (id, password) ->
			val steps = stepsOf(id)
			val toggles = steps.withIndex().filter { (_, step) ->
				step is Step.Tap && step.q == Query.Tag(AuthUiTags.PasswordToggle)
			}
			val shown = steps.indexOfFirst { it is Step.WaitVisible && it.q == Query.Text(password) }

			assertEquals(2, toggles.size, id)
			assertTrue(shown in (toggles.first().index + 1) until toggles.last().index, id)
		}
	}

	private companion object {
		const val AFTER_TOGGLE = 2
	}
}
