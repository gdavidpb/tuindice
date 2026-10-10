package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.debug.DebugLaunchArguments
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import com.gdavidpb.tuindice.scenarios.MockJson.array
import com.gdavidpb.tuindice.scenarios.MockJson.string
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import com.gdavidpb.tuindice.scenarios.shared.SIGN_IN_GROUP
import kotlinx.serialization.json.JsonObject

/**
 * The rules the plan sets on how a scenario is written, as functions that return what breaks them, so
 * `CatalogRulesTest` runs each on the catalog (nothing breaks it) and on a scenario made to break it.
 */
internal object CatalogRules {
	/** Scenarios whose platform branches differ from their budget, in either direction. */
	fun branchBudgetMismatches(scenarios: List<Scenario>, budgetOf: (String) -> Int): List<String> =
		scenarios.mapNotNull { scenario ->
			val branches = scenario.steps.flattened().count { it is Step.OnPlatform }

			"${scenario.id}: $branches branches, budget ${budgetOf(scenario.id)}".takeIf { branches != budgetOf(scenario.id) }
		}

	/** `back()` exists on Android only: it must sit under `onPlatform(Android)` or in an Android-only scenario. */
	fun backOutsideAndroid(scenarios: List<Scenario>): List<String> =
		scenarios.filter { scenario ->
			val androidOnly = scenario.platforms == listOf(Platform.Android)

			scenario.steps.located().any { located ->
				located.step is Step.Back && located.platform != Platform.Android && !(androidOnly && located.platform == null)
			}
		}.map { it.id }

	/** Scenarios that do not run on every platform and are not allowed to. */
	fun restrictedPlatformsOutside(scenarios: List<Scenario>, allowed: Set<String>): List<String> =
		scenarios.filter { it.platforms != Platform.entries && it.id !in allowed }.map { it.id }

	/** `scenario: text` for every text query whose text is not one of [allowed]. */
	fun textsOutside(scenarios: List<Scenario>, allowed: Set<String>): List<String> =
		scenarios.flatMap { scenario ->
			scenario.steps.flattened().flatMap(::queriesOf).filterIsInstance<Query.Text>()
				.filter { it.value !in allowed }.map { "${scenario.id}: '${it.value}'" }
		}.distinct()

	/** `scenario: milliseconds` for every wait whose timeout is not one of the kit's names. */
	fun timeoutsOutsideNames(scenarios: List<Scenario>): List<String> {
		val named = setOf(
			Timeouts.Probe,
			Timeouts.Assert,
			Timeouts.Action,
			Timeouts.Wait,
			Timeouts.Long,
			Timeouts.Sync
		)

		return scenarios.flatMap { scenario ->
			scenario.steps.flattened().flatMap(::timeoutsOf).filter { it !in named }.map { "${scenario.id}: ${it}ms" }
		}.distinct()
	}

	/** `scenario: step` for every step that does not know the DSL line that built it. */
	fun stepsWithoutSite(scenarios: List<Scenario>): List<String> =
		scenarios.flatMap { scenario ->
			scenario.steps.flattened().filter { it.site == null }.map { "${scenario.id}: ${it::class.simpleName} ${it.target}" }
		}

	/** Accounts a scenario starts seeded as and no sync mapping answers, by password or by access token. */
	fun seededAccountsWithoutSyncStub(
		scenarios: List<Scenario>,
		accounts: List<E2eAccount>,
		mappings: List<JsonObject>
	): List<String> {
		val syncs = mappings.filter {
			it.string("request", "urlPath") == SYNC_PATH && it.string("request", "method") == "POST"
		}

		return scenarios.filter { DebugLaunchArguments.SEED_SESSION_ID in it.start.arguments }
			.map { scenario -> accounts.single { it.id == scenario.account } }
			.filterNot { hasSyncStub(it, syncs) }
			.map { it.id }.distinct()
	}

	/**
	 * Scenarios that type the credential (USB id or password) and submit it outside `signInThroughUi`, which is
	 * where the credential is checked against the backend. Typing alone is what the conformance scenarios do to
	 * measure a driver, and tapping alone is a retry; only the pair is a login.
	 */
	fun typedLoginOutsideTheHelper(scenarios: List<Scenario>): List<String> =
		scenarios.filter { scenario ->
			val outside = scenario.steps.located().filter { SIGN_IN_GROUP !in it.groups }.map { it.step }
			val types = outside.any { step ->
				step is Step.EnterText &&
					(step.q == Query.Tag(AuthUiTags.UsbIdTextField) || step.q == Query.Tag(AuthUiTags.PasswordTextField))
			}
			val submits = outside.any { it is Step.Tap && it.q == Query.Tag(AuthUiTags.SignInButton) }

			types && submits
		}.map { it.id }

	private fun hasSyncStub(account: E2eAccount, syncs: List<JsonObject>): Boolean =
		syncs.any { sync ->
			val patterns = sync.string("request", "headers", "Authorization", "equalTo")
			val byToken = account.session?.let { patterns == "Bearer ${it.accessToken}" } == true
			val byPassword = (sync["request"] as? JsonObject)?.array("bodyPatterns").orEmpty().any {
				(it as? JsonObject)?.string("matchesJsonPath")?.contains("== '${account.password}'") == true
			}
			val byScenario = account.mockScenario != null &&
				sync.string("scenarioName") == account.mockScenario &&
				sync.string("requiredScenarioState") == TOKENS_ISSUED

			byToken || byPassword || byScenario
		}

	private const val SYNC_PATH = "/record/v5/sync"
	private const val TOKENS_ISSUED = "TokensIssued"
}
