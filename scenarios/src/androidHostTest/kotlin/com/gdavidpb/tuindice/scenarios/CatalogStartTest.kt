package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.shared.SIGN_IN_GROUP
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** How scenarios begin: seeded as a known account, or clean; and the few that sign in by typing. */
class CatalogStartTest {
	private val scenarios = E2eCatalog.all

	/**
	 * The only scenarios that may type a credential in the sign-in screen; every other one starts seeded.
	 * `record-schedule-view-remembered` starts seeded, signs out and signs in again.
	 */
	private val uiSignInAllowlist = setOf(
		"auth-login-success",
		"auth-login-usb-email",
		"auth-login-usb-email-usbid",
		"auth-usage-data-consent",
		"auth-login-invalid",
		"auth-login-disabled",
		"auth-login-retry-after-unavailable",
		"auth-login-cancel",
		"auth-login-outdated-app",
		"record-schedule-view-remembered"
	)

	private val seededAtStart = setOf("record-schedule-view-remembered")

	private val seedKeys = setOf(
		DebugLaunchArguments.SEED_SESSION_ID,
		DebugLaunchArguments.SEED_ACCESS_TOKEN,
		DebugLaunchArguments.SEED_REFRESH_TOKEN,
		DebugLaunchArguments.SEED_USB_ID,
		DebugLaunchArguments.SEED_PASSWORD,
		DebugLaunchArguments.SEED_COACHMARKS,
		DebugLaunchArguments.MAIN_SECTION
	)

	@Test
	fun theAllowlistIsTheTenScenariosThePlanAllows() {
		assertEquals(ALLOWLIST_SIZE, uiSignInAllowlist.size)
		assertEquals(AUTH_ALLOWLIST_SIZE, uiSignInAllowlist.count { it.startsWith("auth-") })
	}

	@Test
	fun onlyAllowlistedScenariosTypeTheCredential() {
		val offenders = scenarios.filter { it.id !in uiSignInAllowlist && (it.signsIn || usesUiSignIn(it)) }

		assertTrue(offenders.isEmpty(), "scenarios that sign in through the UI unallowed: ${offenders.map { it.id }}")
	}

	@Test
	fun aScenarioThatSignsInDeclaresItAndTheOtherWayAround() {
		scenarios.filter { it.id in uiSignInAllowlist }.forEach { scenario ->
			assertEquals(usesUiSignIn(scenario), scenario.signsIn, "${scenario.id}: signsIn() and signInThroughUi disagree")
		}
	}

	@Test
	fun theAllowlistIsComplete() {
		val ids = scenarios.map { it.id }.toSet()
		val missing = uiSignInAllowlist.filter { it !in ids }

		assertTrue(missing.isEmpty(), "allowlisted scenarios missing from the catalog: $missing")
	}

	@Test
	fun cleanScenariosCarryNoSeedAndSeededOnesAreSeededAsTheirAccount() {
		scenarios.forEach { scenario ->
			val arguments = scenario.start.arguments
			val seeded = DebugLaunchArguments.SEED_SESSION_ID in arguments

			if (seeded) {
				val account = accountOf(scenario)

				assertEquals(account.session?.sessionId, arguments[DebugLaunchArguments.SEED_SESSION_ID], scenario.id)
				assertEquals(account.session?.accessToken, arguments[DebugLaunchArguments.SEED_ACCESS_TOKEN], scenario.id)
				assertEquals(account.session?.refreshToken, arguments[DebugLaunchArguments.SEED_REFRESH_TOKEN], scenario.id)
				assertEquals(account.usbIdFormatted, arguments[DebugLaunchArguments.SEED_USB_ID], scenario.id)
				assertEquals(account.password, arguments[DebugLaunchArguments.SEED_PASSWORD], scenario.id)
				assertEquals(
					listOfNotNull(account.mockScenario).map { it to "TokensIssued" } +
						ExtraMockStatesAtStart.of(scenario.id),
					scenario.start.mockStates.map { it.scenario to it.state },
					scenario.id
				)
			} else {
				assertTrue(arguments.keys.none { it in seedKeys }, "${scenario.id} is clean but carries a seed argument")
				assertTrue(scenario.start.mockStates.isEmpty(), "${scenario.id} is clean but sets mock states")
			}
		}
	}

	@Test
	fun scenariosThatTypeTheCredentialStartCleanExceptTheOneThatSignsOutFirst() {
		scenarios.filter { it.signsIn }.forEach { scenario ->
			val seeded = DebugLaunchArguments.SEED_SESSION_ID in scenario.start.arguments

			assertEquals(scenario.id in seededAtStart, seeded, scenario.id)
		}
	}

	@Test
	fun everyDeclaredAccountIsOneOfTheFixtureAccounts() {
		val ids = E2eAccounts.all.map { it.id }.toSet()

		scenarios.forEach { scenario ->
			scenario.account?.let { assertTrue(it in ids, "${scenario.id} declares unknown account '$it'") }
		}
	}

	@Test
	fun aScenarioThatTypesTheCredentialNamesItsAccount() {
		scenarios.filter { it.signsIn }.forEach { scenario ->
			assertTrue(scenario.account != null, "${scenario.id} signs in but declares no account")
		}
	}

	private fun usesUiSignIn(scenario: Scenario): Boolean =
		scenario.steps.flattened().any { it is Step.Group && it.name == SIGN_IN_GROUP }

	private fun accountOf(scenario: Scenario): E2eAccount {
		val id = checkNotNull(scenario.account) { "${scenario.id} starts seeded but declares no account" }

		return E2eAccounts.all.single { it.id == id }
	}

	private companion object {
		const val ALLOWLIST_SIZE = 10
		const val AUTH_ALLOWLIST_SIZE = 9
	}
}
