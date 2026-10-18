package com.gdavidpb.tuindice.ui.activity

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TermReportPushDecisionTest {
	@Test
	fun when_aFreshLaunchCarriesThePush_then_theReportOpens() {
		assertTrue(shouldOpenTermReportOnCreate(isRestored = false, isLaunchedFromHistory = false))
	}

	@Test
	fun when_theActivityIsRestored_then_theReportIsNotAskedAgain() {
		assertFalse(shouldOpenTermReportOnCreate(isRestored = true, isLaunchedFromHistory = false))
	}

	@Test
	fun when_theIntentComesFromHistory_then_theReportIsNotOpenedAgain() {
		assertFalse(shouldOpenTermReportOnCreate(isRestored = false, isLaunchedFromHistory = true))
	}

	@Test
	fun when_bothApply_then_theReportIsNotOpened() {
		assertFalse(shouldOpenTermReportOnCreate(isRestored = true, isLaunchedFromHistory = true))
	}
}
