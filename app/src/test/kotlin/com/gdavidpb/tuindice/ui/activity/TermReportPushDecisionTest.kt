package com.gdavidpb.tuindice.ui.activity

import com.gdavidpb.tuindice.base.domain.model.TermReportPush
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

	@Test
	fun when_aTermReportPushPayloadIsOpened_then_itsInsightIsTheRoute() {
		val payload = mapOf(
			TermReportPush.TYPE_KEY to TermReportPush.TERM_REPORT_TYPE,
			TermReportPush.INSIGHT_ID_KEY to "insight-1"
		)

		assertEquals(
			"insight-1",
			termReportInsightIdOrNull(
				type = payload[TermReportPush.TYPE_KEY],
				insightId = payload[TermReportPush.INSIGHT_ID_KEY]
			)
		)
	}

	@Test
	fun when_thePushIsNotATermReport_then_noInsightIsOpened() {
		assertNull(termReportInsightIdOrNull(type = "another_type", insightId = "insight-1"))
	}

	@Test
	fun when_thePushHasNoType_then_noInsightIsOpened() {
		assertNull(termReportInsightIdOrNull(type = null, insightId = "insight-1"))
	}

	@Test
	fun when_aTermReportPushNamesNoInsight_then_noInsightIsOpened() {
		assertNull(termReportInsightIdOrNull(type = TermReportPush.TERM_REPORT_TYPE, insightId = null))
	}
}
