package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.scenarios.MockJson.string
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.Start
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** The session an account is seeded with must be the one its mock scenario hands out. */
class AccountSessionFixturesTest {
	private val accounts = E2eAccounts.all
	private val canonicalUsbId = Regex("""\d{2}-\d{5}""")

	@Test
	fun sessionAccountsHaveAMockScenarioAndUiOnlyAccountsDoNot() {
		accounts.forEach { account ->
			assertEquals(account.session != null, account.mockScenario != null, account.id)
		}
	}

	@Test
	fun everySessionAccountSignsInThroughItsOwnScenarioFromTheStartedState() {
		accounts.filter { it.session != null }.forEach { account ->
			val bootstrap = MockJson.objects(RepoFiles.loginMappings)
				.filter { it.string("request", "urlPath") == "/auth/v2/bootstrap" }
				.filter {
					it.string("request", "headers", "Authorization", "equalTo") == "Basic ${AccountCredentials.basic(account)}"
				}
				.filter { it.string("scenarioName") == account.mockScenario }
				.filter { it.string("requiredScenarioState") == "Started" }

			assertEquals(1, bootstrap.size, "expected one bootstrap mapping of ${account.id} in ${account.mockScenario}")
			assertEquals("200", bootstrap.single().string("response", "status"), account.id)
		}
	}

	@Test
	fun sessionValuesAreThoseOfTheExchangeMapping() {
		accounts.filter { it.session != null }.forEach { account ->
			val session = checkNotNull(account.session)
			val exchanges = MockJson.objects(RepoFiles.loginMappings)
				.filter { it.string("request", "urlPath") == "/auth/v2/token/exchange" }
				.filter { it.string("scenarioName") == account.mockScenario }
				.filter { it.string("response", "jsonBody", "usb_id") == account.usbIdFormatted }

			assertEquals(1, exchanges.size, "expected one exchange mapping of ${account.id}, found ${exchanges.size}")

			val body = exchanges.single()
			assertEquals(session.sessionId, body.string("response", "jsonBody", "session_id"), account.id)
			assertEquals(session.accessToken, body.string("response", "jsonBody", "access_token"), account.id)
			assertEquals(session.refreshToken, body.string("response", "jsonBody", "refresh_token"), account.id)
		}
	}

	@Test
	fun theMockScenarioAndItsSeededStateExistInTheMappings() {
		val all = MockJson.objects(RepoFiles.allMappings)

		accounts.mapNotNull { it.mockScenario }.toSet().forEach { scenario ->
			val ofScenario = all.filter { it.string("scenarioName") == scenario }

			assertTrue(ofScenario.isNotEmpty(), "no mapping declares the scenario '$scenario'")
			assertTrue(
				ofScenario.any {
					it.string("requiredScenarioState") == "TokensIssued" || it.string("newScenarioState") == "TokensIssued"
				},
				"scenario '$scenario' never reaches the TokensIssued state"
			)
		}
	}

	@Test
	fun seededLaunchesCarryTheCanonicalUsbIdAndTheSessionTheAppParses() {
		accounts.filter { it.session != null }.forEach { account ->
			val arguments = Start.Seeded(account).toLaunchSpec().arguments
			val seed = checkNotNull(DebugLaunchArguments.parse(arguments).sessionSeed)

			assertTrue(canonicalUsbId.matches(arguments.getValue(DebugLaunchArguments.SEED_USB_ID)))
			assertEquals(account.usbIdFormatted, seed.usbId)
			assertEquals(account.password, seed.password)
			assertEquals(checkNotNull(account.session).accessToken, seed.accessToken)
		}
	}

	@Test
	fun anAccountWithoutSessionCannotBeSeeded() {
		accounts.filter { it.session == null }.forEach { account ->
			assertFailsWith<IllegalArgumentException>(account.id) { Start.Seeded(account) }
		}
	}
}
