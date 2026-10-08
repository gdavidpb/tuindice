package com.gdavidpb.tuindice.scenarios

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.expectRequest
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.model.LaunchSpec
import com.gdavidpb.tuindice.scenarios.catalog.E2eCatalog
import com.gdavidpb.tuindice.scenarios.shared.Within
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A pending change sent at sign-out is shown accepted by the server, not only sent. */
class FlushAcceptedRulesTest {
	private val scenarios = E2eCatalog.all
	private val clean = LaunchSpec(emptyMap())

	/** The scenarios in which a pending change is sent while signing out and the server takes it. */
	private val flushing = setOf("auth-pending-sign-out-flush-success", "auth-update-password")

	@Test
	fun theScenariosThatFlushAPendingChangeShowTheServerAcceptingIt() {
		val offenders = CatalogToleranceRules.flushesNotShownAccepted(scenarios, flushing)

		assertTrue(offenders.isEmpty(), "flush scenarios that do not wait for the accepted resend: $offenders")
		assertEquals(flushing, scenarios.map { it.id }.filter { it in flushing }.toSet())
	}

	@Test
	fun aFlushThatOnlyLooksForTheRequestOrNeverChecksTheOfferIsCaught() {
		val accepted = scenario("x-ok", "x", clean) {
			expectRequest("POST", "/evaluations/v3", status = 200)
			waitGone(AuthUiTags.SignOutSecondaryButton, Within.Assert)
		}
		val noStatus = scenario("x-no-status", "x", clean) {
			expectRequest("POST", "/evaluations/v3")
			waitGone(AuthUiTags.SignOutSecondaryButton, Within.Assert)
		}
		val refused = scenario("x-503", "x", clean) {
			expectRequest("POST", "/evaluations/v3", status = 503)
			waitGone(AuthUiTags.SignOutSecondaryButton, Within.Assert)
		}
		val noOfferCheck = scenario("x-offer", "x", clean) { expectRequest("POST", "/evaluations/v3", status = 200) }
		val ids = setOf("x-ok", "x-no-status", "x-503", "x-offer")

		assertEquals(
			listOf("x-no-status", "x-503", "x-offer"),
			CatalogToleranceRules.flushesNotShownAccepted(listOf(accepted, noStatus, refused, noOfferCheck), ids)
		)
	}
}
