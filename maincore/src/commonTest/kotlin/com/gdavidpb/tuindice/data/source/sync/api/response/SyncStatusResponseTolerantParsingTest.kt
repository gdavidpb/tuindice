package com.gdavidpb.tuindice.data.source.sync.api.response

import com.gdavidpb.tuindice.di.createSharedJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SyncStatusResponseTolerantParsingTest {
	private val json = createSharedJson()

	@Test
	fun unknownStatusValues_decodeToUnknownInsteadOfFailingTheParse() {
		val report = json.decodeFromString<SyncReportResponse>(
			"""{"status":"brand_new","sources":{"record":{"status":"success"},""" +
				""""enrollment":{"status":"something_else"}}}"""
		)

		assertEquals(SyncReportStatusResponse.Unknown, report.status)
		assertEquals(SyncSourceStatusResponse.Success, report.sources.record.status)
		assertEquals(SyncSourceStatusResponse.Unknown, report.sources.enrollment.status)
	}

	@Test
	fun notEnrolled_decodesWithItsSituation_andIsOptionalOtherwise() {
		val report = json.decodeFromString<SyncReportResponse>(
			"""{"status":"success","sources":{"record":{"status":"success"},""" +
				""""enrollment":{"status":"not_enrolled","situation":{"code":"15"}}}}"""
		)

		assertEquals(SyncSourceStatusResponse.NotEnrolled, report.sources.enrollment.status)
		assertEquals("15", report.sources.enrollment.situation?.code)
		assertNull(report.sources.enrollment.situation?.description)
		assertNull(report.sources.record.situation)
	}

	// The deployed backend keeps the term in the record when it annuls provisionally and drops it
	// when it is definitive; the sync report itself is identical, so both shapes decode the same.
	@Test
	fun situationOnASuccessSource_decodesForBothAnnulmentMoments() {
		val report = json.decodeFromString<SyncReportResponse>(
			"""{"status":"success","sources":{"record":{"status":"success"},""" +
				""""enrollment":{"status":"success","situation":{"code":"01","description":"ANULADA"}}}}"""
		)

		assertEquals("ANULADA", report.sources.enrollment.situation?.description)
	}
}
