package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.debug.DebugSessionSeed
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.Start
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.File
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** The accounts the scenarios sign in with must be the ones the WireMock mappings accept. */
class AccountFixturesTest {
	private val accounts = E2eAccounts.all
	private val canonicalUsbId = Regex("""\d{2}-\d{5}""")

	@Test
	fun accountIdsAreUnique() {
		assertEquals(accounts.size, accounts.map { it.id }.toSet().size)
	}

	@Test
	fun everyCredentialIsAcceptedByALoginMapping() {
		val loginMappings = RepoFiles.loginMappings.listFiles { file -> file.extension == "json" }.orEmpty()
			.map { it.readText() }

		accounts.forEach { account ->
			val header = Regex(""""equalTo"\s*:\s*"Basic ${Regex.escape(basicCredential(account))}"""")

			assertTrue(
				loginMappings.any { header.containsMatchIn(it) },
				"no mapping under mocks/mappings/login accepts ${account.usbIdFormatted}:${account.password} (${account.id})"
			)
		}
	}

	@Test
	fun sessionValuesAreThoseOfTheExchangeMapping() {
		accounts.filter { it.session != null }.forEach { account ->
			val session = checkNotNull(account.session)
			val exchanges = mappings(RepoFiles.loginMappings)
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
		val all = mappings(RepoFiles.allMappings)

		accounts.mapNotNull { it.mockScenario }.forEach { scenario ->
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
		}
	}

	@Test
	fun theCanonicalSessionMatchesTheLegacySeed() {
		val session = checkNotNull(E2eAccounts.Canonical.session)
		val legacy = DebugSessionSeed.Canonical

		assertEquals(legacy.sessionId, session.sessionId)
		assertEquals(legacy.accessToken, session.accessToken)
		assertEquals(legacy.refreshToken, session.refreshToken)
		assertEquals(legacy.usbId, E2eAccounts.Canonical.usbIdFormatted)
		assertEquals(legacy.password, E2eAccounts.Canonical.password)
	}

	@Test
	fun anAccountWithoutSessionCannotBeSeeded() {
		assertFailsWith<IllegalArgumentException> { Start.Seeded(E2eAccounts.LoginCancel) }
	}

	private fun basicCredential(account: E2eAccount): String =
		Base64.getEncoder().encodeToString("${account.usbIdFormatted}:${account.password}".toByteArray())

	private fun mappings(directory: File): List<JsonObject> =
		directory.walkTopDown().filter { it.isFile && it.extension == "json" }
			.map { Json.parseToJsonElement(it.readText()) }
			.filterIsInstance<JsonObject>()
			.toList()

	private fun JsonObject.string(vararg path: String): String? {
		var node: JsonObject = this

		path.dropLast(1).forEach { key -> node = node[key] as? JsonObject ?: return null }

		return (node[path.last()] as? JsonPrimitive)?.content
	}
}
