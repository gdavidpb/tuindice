package com.gdavidpb.tuindice.platform.android

import com.gdavidpb.tuindice.base.domain.model.TermReportPush
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TermReportNotificationTest {
	@Test
	fun when_thePushIsATermReport_then_itsInsightIsTheOneShown() {
		val data = mapOf(
			TermReportPush.TYPE_KEY to TermReportPush.TERM_REPORT_TYPE,
			TermReportPush.INSIGHT_ID_KEY to "term-1:2"
		)

		assertEquals("term-1:2", termReportInsightIdOf(data))
	}

	@Test
	fun when_thePushIsAnotherType_then_nothingIsShown() {
		val data = mapOf(TermReportPush.TYPE_KEY to "reminder", TermReportPush.INSIGHT_ID_KEY to "term-1:2")

		assertNull(termReportInsightIdOf(data))
	}

	@Test
	fun when_theTermReportPushDoesNotNameTheReport_then_nothingIsShown() {
		assertNull(termReportInsightIdOf(mapOf(TermReportPush.TYPE_KEY to TermReportPush.TERM_REPORT_TYPE)))
		assertNull(
			termReportInsightIdOf(
				mapOf(TermReportPush.TYPE_KEY to TermReportPush.TERM_REPORT_TYPE, TermReportPush.INSIGHT_ID_KEY to "")
			)
		)
	}

	@Test
	fun when_theExtrasAreBuilt_then_theyAreTheKeysTheTapReads() {
		assertEquals(
			mapOf(
				TermReportPush.TYPE_KEY to TermReportPush.TERM_REPORT_TYPE,
				TermReportPush.INSIGHT_ID_KEY to "term-1:2"
			),
			termReportExtrasOf(insightId = "term-1:2")
		)
	}

	@Test
	fun when_thePermissionOrTheSettingIsOff_then_thePushIsNotShown() {
		assertTrue(canShowTermReportNotification(isPermissionGranted = true, areNotificationsEnabled = true))
		assertFalse(canShowTermReportNotification(isPermissionGranted = false, areNotificationsEnabled = true))
		assertFalse(canShowTermReportNotification(isPermissionGranted = true, areNotificationsEnabled = false))
	}
}
