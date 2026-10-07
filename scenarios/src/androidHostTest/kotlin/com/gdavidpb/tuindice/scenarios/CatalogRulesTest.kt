package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.back
import com.gdavidpb.tuindice.scenariokit.dsl.enterSecureText
import com.gdavidpb.tuindice.scenariokit.dsl.enterText
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Step
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.catalog.PlatformBranchBudget
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.scenarios.shared.signInThroughUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * The rules of how a scenario is written (see `CatalogRules`), each checked on the catalog and then on a
 * scenario fabricated to break it, so a rule that stops detecting anything fails here.
 */
class CatalogRulesTest {
	private val scenarios = E2eCatalog.all
	private val clean = LaunchSpec(emptyMap())

	/** Scenarios that run on one platform on purpose: the conformance one of `back`, which iOS does not have. */
	private val platformRestricted = setOf("conformance-back")

	private val allowedTexts: Set<String> =
		Copy.bindings.map { it.text }.toSet() +
			E2eFixtures.all.map { it.value } +
			// A field shows back the password it was typed with.
			E2eAccounts.all.map { it.password }

	// The platform branches match the budget exactly.

	@Test
	fun everyScenarioHoldsExactlyTheBranchesOfItsBudget() {
		val mismatches = CatalogRules.branchBudgetMismatches(scenarios, PlatformBranchBudget::of)

		assertTrue(mismatches.isEmpty(), "branches and budget differ: $mismatches")
	}

	@Test
	fun theBranchBudgetCatchesSlackAndExcess() {
		val branching = scenario("x-one", "x", clean) { onPlatform(Platform.Android) { tap("a") } }
		val plain = scenario("x-none", "x", clean) { tap("a") }

		assertEquals(1, CatalogRules.branchBudgetMismatches(listOf(branching), { 0 }).size, "a branch over a zero budget")
		assertEquals(1, CatalogRules.branchBudgetMismatches(listOf(plain), { 1 }).size, "a budget nobody uses")
		assertTrue(CatalogRules.branchBudgetMismatches(listOf(branching, plain), { if (it == "x-one") 1 else 0 }).isEmpty())
	}

	// Back is Android only.

	@Test
	fun backOnlyRunsOnAndroid() {
		val offenders = CatalogRules.backOutsideAndroid(scenarios)

		assertTrue(offenders.isEmpty(), "scenarios that press back where iOS has none: $offenders")
	}

	@Test
	fun backOutsideAnAndroidBranchIsCaught() {
		val everywhere = scenario("x-back", "x", clean) { back() }
		val underIos = scenario("x-ios", "x", clean) { onPlatform(Platform.Ios) { back() } }
		val underAndroid = scenario("x-android", "x", clean) { onPlatform(Platform.Android) { back() } }
		val androidOnly = scenario("x-only", "x", clean) {
			platforms(Platform.Android)
			back()
		}

		assertEquals(
			listOf("x-back", "x-ios"),
			CatalogRules.backOutsideAndroid(listOf(everywhere, underIos, underAndroid, androidOnly))
		)
	}

	// Platform restriction has a list.

	@Test
	fun onlyTheListedScenariosSkipAPlatform() {
		val offenders = CatalogRules.restrictedPlatformsOutside(scenarios, platformRestricted)

		assertTrue(offenders.isEmpty(), "scenarios that drop a platform without being listed: $offenders")
		assertTrue(
			scenarios.filter { it.platforms != Platform.entries }.map { it.id }.toSet() == platformRestricted,
			"the list names a scenario that runs everywhere"
		)
	}

	@Test
	fun aScenarioThatDropsAPlatformIsCaught() {
		val android = scenario("x-android", "x", clean) {
			platforms(Platform.Android)
			tap("a")
		}

		assertEquals(listOf("x-android"), CatalogRules.restrictedPlatformsOutside(listOf(android), emptySet()))
		assertTrue(CatalogRules.restrictedPlatformsOutside(listOf(android), setOf("x-android")).isEmpty())
	}

	// Texts come from Copy and the fixtures.

	@Test
	fun everyTextQueryComesFromCopyOrTheFixtures() {
		val offenders = CatalogRules.textsOutside(scenarios, allowedTexts)

		assertTrue(offenders.isEmpty(), "texts that are neither Copy nor fixtures: $offenders")
	}

	@Test
	fun aLiteralTextIsCaught() {
		val literal = scenario("x-text", "x", clean) { waitVisible(text("Entrar"), 5.seconds) }
		val named = scenario("x-copy", "x", clean) { waitVisible(text(Copy.NoticeTitle), 5.seconds) }

		assertEquals(listOf("x-text: 'Entrar'"), CatalogRules.textsOutside(listOf(literal, named), allowedTexts))
	}

	// Timeouts have names.

	@Test
	fun everyTimeoutIsOneOfTheKitNames() {
		val offenders = CatalogRules.timeoutsOutsideNames(scenarios.filterNot { it.module == "poc" })

		assertTrue(offenders.isEmpty(), "timeouts that are not Timeouts.*: $offenders")
	}

	@Test
	fun anArbitraryTimeoutIsCaught() {
		val arbitrary = scenario("x-wait", "x", clean) { waitVisible("a", 2.seconds) }
		val named = scenario("x-named", "x", clean) { waitVisible("a", Within.Action) }

		assertEquals(listOf("x-wait: 2000ms"), CatalogRules.timeoutsOutsideNames(listOf(arbitrary, named)))
	}

	// Every step knows its line.

	@Test
	fun everyStepKnowsTheDslLineThatBuiltIt() {
		val offenders = CatalogRules.stepsWithoutSite(scenarios)

		assertTrue(offenders.isEmpty(), "steps without a call site: ${offenders.take(MAX_SHOWN)}")
	}

	@Test
	fun aStepWithoutASiteIsCaught() {
		val handmade = Scenario("x-handmade", "x", clean, steps = listOf(Step.Back()))

		assertEquals(1, CatalogRules.stepsWithoutSite(listOf(handmade)).size)
		assertTrue(CatalogRules.stepsWithoutSite(listOf(scenario("x-dsl", "x", clean) { back() })).isEmpty())
	}

	// Every seeded account has a sync stub.

	@Test
	fun everySeededAccountHasASyncStub() {
		val missing = CatalogRules.seededAccountsWithoutSyncStub(
			scenarios,
			E2eAccounts.all,
			MockJson.objects(RepoFiles.allMappings)
		)

		assertTrue(missing.isEmpty(), "accounts the sync mock does not answer: $missing")
	}

	@Test
	fun aSeededAccountWithoutAStubIsCaught() {
		val lone = scenario("x-seeded", "x", Start.Seeded(E2eAccounts.RecordTermRejected).toLaunchSpec()) {
			account(E2eAccounts.RecordTermRejected.id)
		}

		assertEquals(
			listOf(E2eAccounts.RecordTermRejected.id),
			CatalogRules.seededAccountsWithoutSyncStub(listOf(lone), E2eAccounts.all, emptyList())
		)
		assertTrue(
			CatalogRules.seededAccountsWithoutSyncStub(
				listOf(lone),
				E2eAccounts.all,
				MockJson.objects(RepoFiles.allMappings)
			).isEmpty()
		)
	}

	// Typing the login goes through the helper.

	@Test
	fun theCredentialIsOnlyTypedThroughTheSignInHelper() {
		val offenders = CatalogRules.typedLoginOutsideTheHelper(scenarios)

		assertTrue(offenders.isEmpty(), "scenarios that type the login by hand: $offenders")
	}

	@Test
	fun aHandTypedLoginIsCaughtAndTheHelperTypingAndATapAloneAreNot() {
		val byHand = scenario("x-hand", "x", clean) {
			enterText(AuthUiTags.UsbIdTextField, "1111111")
			enterSecureText(AuthUiTags.PasswordTextField, "123456")
			tap(AuthUiTags.SignInButton)
		}
		val passwordOnly = scenario("x-password", "x", clean) {
			enterSecureText(AuthUiTags.PasswordTextField, "123456")
			tap(AuthUiTags.SignInButton)
		}
		val typingOnly = scenario("x-typing", "x", clean) { enterText(AuthUiTags.UsbIdTextField, "1111111") }
		val tapOnly = scenario("x-tap", "x", clean) { tap(AuthUiTags.SignInButton) }
		val helper = scenario("x-helper", "x", clean) { signInThroughUi(E2eAccounts.Canonical) }
		val helperThenRetry = scenario("x-retry", "x", clean) {
			signInThroughUi(E2eAccounts.Canonical)
			tap(AuthUiTags.SignInButton)
		}

		assertEquals(
			listOf("x-hand", "x-password"),
			CatalogRules.typedLoginOutsideTheHelper(
				listOf(byHand, passwordOnly, typingOnly, tapOnly, helper, helperThenRetry)
			)
		)
	}

	private companion object {
		const val MAX_SHOWN = 5
	}
}
